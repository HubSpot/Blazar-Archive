/*global config*/
import Reflux from 'reflux';

import {find, has, extend} from 'underscore'; 
import BuildStates from '../constants/BuildStates';
import {buildIsOnDeck, buildIsInactive} from '../components/Helpers';

import BuildActions from '../actions/buildActions';
import BuildHistoryActions from '../actions/buildHistoryActions';

import BranchDefinition from '../models/BranchDefinition';
import BranchModules from '../collections/BranchModules';
import Build from '../models/Build';
import Log from '../models/Log';
import LogSize from '../models/LogSize';
import BuildTrigger from '../models/BuildTrigger';


const BuildStore = Reflux.createStore({

  listenables: BuildActions,

  init() {
    this.build = {};
    this.params = {};
  },
  
  loadBuildError(error) {
    this.trigger({
      error: error,
      loading: false
    });
  },

  onLoadBuild(params) {
    this.params = params;
    
    this._getBranchId()
      .then(this._getModule)
      .then(this._getBuild)
      .then(this._getLog)
      .then(this._assignBuildProcessing);
  },

  onCancelBuild() {
    const build = new Build({
      buildId: this.build.model.data.build.id
    });
    
    const cancel = build.cancel();
    
    cancel.done((data, textStatus, jqXHR) => {
      if (jqXHR.status === 204) {
        this.build.model.data.build.state = BuildStates.CANCELLED;
        this.trigger({
          build: this.build.model.data
        });
      }
    });

    cancel.error((err) => {
      this.loadBuildError(`Error cancelling build #${this.params.buildNumber}. See your console for more detail.`);
    });
  },
  
  onResetBuild() {
    this.init();
  },

  onTriggerBuild(moduleId) {
    this.trigger({
      buildTriggeringDone: false
    });
    
    const trigger = new BuildTrigger({
      moduleId: moduleId
    });

    trigger.fetch()
      .done(() => {
        this.trigger({
          buildTriggeringDone: true
        });
        BuildHistoryActions.fetchLatestHistory();
      })

      .fail((data, textStatus, jqXHR) => {
        this.trigger({
          buildTriggeringDone: true,
          buildTriggeringError: jqXHR
        });
      });
  },

  onNavigationChange(position) {
    const startOfLogLoaded = this.build.logCollection.minOffsetLoaded === 0;
    const endOfLogLoaded = this.build.logCollection.maxOffsetLoaded === this.build.logCollection.options.size;
    const buildInProgress = this.build.model.data.build.state === BuildStates.IN_PROGRESS;

    // navigated to the top
    if (position === 'top') {
      
      if (buildInProgress) {
        this.onSetLogPollingState(false);
      }

      if (startOfLogLoaded) {
        this._triggerUpdate({positionChange: position});  
      }
      
      else {
        this._resetLogViaNavigation(position);
      }
    }
  
    // navigated to the bottom
    else if (position === 'bottom') {

      if (buildInProgress) {
        this.onSetLogPollingState(true);
      }

      if (endOfLogLoaded) {
        this._triggerUpdate({positionChange: position});
      }

      else {
        this._resetLogViaNavigation(position);
      }      
    }

  },

  onFetchPrevious() {
    const log = this.build.logCollection;
  
    if (log.minOffsetLoaded === 0) {
      this._triggerUpdate();
    }

    else {
      log.fetchPrevious().done(this._triggerUpdate);
    }
  },
  
  onFetchNext() {
    this.build.logCollection
      .fetchNext()
      .done(this._triggerUpdate);
  },

  onSetLogPollingState(state) {
    if (this.build.logCollection) {
      this.build.logCollection.shouldPoll = state;
      // if we stopped polling and are starting up again,
      // fetch the latest log size, last offset, 
      // and start polling again
      if (state) {
        this._getLog().done(this._pollLog);
      }
    }
  },

  _resetLogViaNavigation(position) {
    const logSize = this.build.logCollection.options.size;

    const logPromise = this.build.logCollection.updateLogForNavigationChange({
      size: logSize,
      position: position
    }).fetch();
    
    logPromise.done(() => {
      this._triggerUpdate({positionChange: position});
    });
  },

  _fetchBuild() {
    const buildPromise = this.build.model.fetch();
    
    buildPromise
      .fail((jqXHR) => {
        this.loadBuildError(`Error retrieving build #${this.params.buildNumber}. See your console for more detail.`);
        console.warn(jqXHR);
      });
      
      return buildPromise;
  },

  _fetchLog() {
    const logPromise = this.build.logCollection.fetch();
    
    logPromise
      .fail((jqXHR) => {
        this.loadBuildError(`Error retrieving log for build #${this.params.buildNumber}. See your console for more detail.`);
        console.warn(jqXHR);
      });

    return logPromise;
  },
  
  _pollLog() {
    if (!this.build.logCollection) {
      return;
    }
  
    if (this.build.logCollection.shouldPoll && this.build.model.data.build.state === BuildStates.IN_PROGRESS) {
      this._fetchLog().done((data, textStatus, jqxhr) => {
        this._triggerUpdate();
        this.build.logCollection.requestOffset = data.nextOffset;
        
        setTimeout(() => {
          this._pollLog();
        }, config.activeBuildRefresh);
      });
    }

  },

  _pollBuild() {    
    if (this.build.model.data.build.state === BuildStates.IN_PROGRESS) {
      this._fetchBuild().done((data) => {
        this._triggerUpdate();
        setTimeout(() => {
          this._pollBuild();
        }, config.activeBuildRefresh);
      });
    }
  },

  _processFinishedBuild() {
    this._getLog().then(this._fetchLog).done((data) => {
      this.trigger({
        build: this.build.model.data,
        log: this.build.logCollection,
        loading: false
      });
    });
  },

  _processBuildOnDeck() {
    this._triggerUpdate();  
  },

  _processActiveBuild() { 
    this._pollLog();
    this._pollBuild();    
  },

  _getBranchId() {
    const branchDefinition = new BranchDefinition(this.params);
    const branchPromise =  branchDefinition.fetch();

    branchPromise
      .done(() => {
        this.build.branchId = branchDefinition.data.id;
      })
      .fail(() => {
        this.loadBuildError(`Sorry but we can't find this build. Check that this branch or module still exists.`);
      });

    return branchPromise;
  },
  
  _getModule() {
    const branchModules = new BranchModules({
      branchId: this.build.branchId
    });

    const modulesPromise = branchModules.fetch();

    modulesPromise
      .done((data, textStatus, jqXHR) => {
        this.build.module = find(branchModules.data, (m) => {
          return m.name === this.params.module;
        });

        if (!this.build.module) {
          this.loadBuildError(`Sorry but we can't find any module named ${this.params.module}.`);
        }
      })
      .fail((jqXHR) => {
        this.loadBuildError('Error requesting module ${this.build.module}. See your console for more detail.');
        console.warn(jqXHR);
      });
  
    return modulesPromise;
  },
  
  _getBuild() {
    if (!this.build.module.id) {
      return;
    }

    this.build.model = new Build(this.params);
    this.build.model.moduleId = this.build.module.id;
    return this._fetchBuild();
  },
  
  _getLog() {
    const logSizePromise = this._getLogSize();
    
    if (!logSizePromise) {
      return;
    }
    
    logSizePromise.done((size) => {
      this.build.logCollection = new Log({
        buildNumber: this.build.model.data.build.id,
        size: size,
        buildState: this.build.model.data.build.state
      });    
    });
      
    return logSizePromise;
  },
  
  _getLogSize() {
    if (!this.build.model) {
      return;
    }

    if (buildIsOnDeck(this.build.model.data.build.state)) {
      return;
    }

    const logSize = new LogSize({
      buildNumber: this.build.model.data.build.id
    });

    const sizePromise = logSize.fetch();

    sizePromise.fail((err) => {
      console.warn(err);
      this.loadBuildError('Error requesting log size. View your console for more detail.');
    });

    return sizePromise;
  },

  _assignBuildProcessing() {
    const buildState = this.build.model.data.build.state;
    
    switch (buildState) {
      case BuildStates.QUEUED:
      case BuildStates.LAUNCHING:
        this._processBuildOnDeck();
        break;
    
      case BuildStates.IN_PROGRESS:
        this._processActiveBuild();
        break;
    
      case BuildStates.SUCCEEDED:
      case BuildStates.FAILED: 
      case BuildStates.CANCELLED:
        this._processFinishedBuild();
        break;
    }

  },

  _triggerUpdate(additional = {}) {
    const buildInfo = {
      build: this.build.model.data,
      log: this.build.logCollection || {},
      loading: false,
      positionChange: false
    };

    const buildUpdate = extend(buildInfo, additional);

    this.trigger(buildUpdate);
  },

});

export default BuildStore;
