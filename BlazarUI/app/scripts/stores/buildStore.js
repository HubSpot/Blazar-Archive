/*global config*/
import Reflux from 'reflux';
import BuildActions from '../actions/buildActions';

import $ from 'jquery';
import BranchDefinition from '../models/BranchDefinition';
import BranchModules from '../collections/BranchModules';
import Build from '../models/Build';
import Log from '../models/Log';
import LogSize from '../models/LogSize';

import {find, has, extend} from 'underscore';
import BuildStates from '../constants/BuildStates';


import {buildIsOnDeck, buildIsInactive} from '../components/Helpers';

// TO DO
// import BuildTrigger from '../models/BuildTrigger';
// import BuildHistoryActions from '../actions/buildHistoryActions';


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
  
  onResetBuild() {
    this.init();
  },

  onLoadBuild(params) {
    this.params = params;
    
    this._getBranchId()
      .then(this._getModule)
      .then(this._getBuild)
      .then(this._getLog)
      .then(this._assignBuildProcessing);
  },

  onNavigationChange(position) {
    const startOfLogLoaded = this.build.logCollection.minOffsetLoaded === 0;
    const endOfLogLoaded = this.build.logCollection.maxOffsetLoaded === this.build.logCollection.options.size;

    if (startOfLogLoaded && position === 'top') {
      this.trigger({
        positionChange: position
      });
    }

    else if (endOfLogLoaded && position === 'bottom') {
      this.trigger({
        positionChange: position
      });
    }

    else {
      this._resetLogViaNavigation(position);
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
    const log = this.build.logCollection;

    // have already loaded the end of the log
    if (log.options.size === log.maxOffsetLoaded) {
      this._triggerUpdate();
    }
    
    else {
      log.fetchNext().done(this._triggerUpdate);
    }
  },

  _resetLogViaNavigation(position) {
    const logSize = this.build.logCollection.options.size;

    // To Do:
    // if navigating to the bottom, we need to update our 
    // size calculation if build is still in progress
    // 
    if (this.build.state === BuildStates.IN_PROGRESS) {
      // TO DO
    }

    const logPromise = this.build.logCollection.updateLogForNavigationChange({
      size: logSize,
      position: position
    }).fetch();
    
    logPromise.done(() => {
      this._triggerUpdate({positionChange: position});
    });
  },

  _triggerUpdate(additional = {}) {
    const buildInfo = {
      build: this.build.model.data,
      log: this.build.logCollection,
      loading: false,
      positionChange: false
    };

    this.trigger(extend(buildInfo, additional));
  },
  
  _fetchBuild() {
    const buildPromise = this.build.model.fetch();
    
    buildPromise
      .error((jqXHR) => {
        this.loadBuildError(`Error retrieving build #${this.params.buildNumber}. See your console for more detail.`);
        console.warn(jqXHR);
      });
      
      return buildPromise;
  },
  
  _fetchLog() {
    const logPromise = this.build.logCollection.fetch();
    
    logPromise
      .error(() => {
        this.loadBuildError(`Error retrieving log for build #${this.params.buildNumber}. See your console for more detail.`);
        console.warn(jqXHR);
      });

    return logPromise;
  },

  // TO DO:
  _processBuildOnDeck() {
    // this._pollBuild();
  },
  
  // TO DO:
  _processActiveBuild() {

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

  _getBranchId() {
    const branchDefinition = new BranchDefinition(this.params);
    const branchPromise =  branchDefinition.fetch();

    branchPromise
      .done(() => {
        this.build.branchId = branchDefinition.data.id;
      })
      .error(() => {
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
      .error((jqXHR) => {
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
    
    sizePromise.error((err) => {
      console.warn(err);
      this.loadBuildError('Error requesting log size. View your console for more detail.');
    });

    return sizePromise;
  }

});

export default BuildStore;
