/*global config*/
import Reflux from 'reflux';
import {extend, findWhere} from 'underscore'; 
import BuildStates from '../constants/BuildStates';
import {buildIsOnDeck} from '../components/Helpers';

import Build from '../models/Build';
import Log from '../models/Log';
import LogSize from '../models/LogSize';
import Resource from '../services/ResourceProvider';

class BuildApi {
  
  constructor(params) {
    this.params = params;
    this.build = {};
  }
  
  _buildError(error) {
    this.cb(error);
  }

  loadBuild(cb) {
    this.cb = cb;
    this._getBuild()
      .then(this._getLog.bind(this))
      .then(this._assignBuildProcessing.bind(this));
  }

  navigationChange(position, cb) {
    this.cb = cb;
    const startOfLogLoaded = this.build.logCollection.minOffsetLoaded === 0;
    const endOfLogLoaded = this.build.logCollection.maxOffsetLoaded === this.build.logCollection.options.size;
    const buildInProgress = this.build.model.data.state === BuildStates.IN_PROGRESS;

    // navigated to the top
    if (position === 'top') {

      if (buildInProgress) {
        this.setLogPollingState(false);
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
        this.setLogPollingState(true);
      }

      if (endOfLogLoaded) {
        this._triggerUpdate({positionChange: position});
      }

      else {
        this._resetLogViaNavigation(position);
      }      
    }

  }

  fetchPrevious(cb) {
    if (cb) {
      this.cb = cb;  
    }

    const log = this.build.logCollection;
  
    if (log.minOffsetLoaded === 0) {
      this._triggerUpdate();
    }

    else {
      log.fetchPrevious().done(this._triggerUpdate.bind(this));
    }
  }
  
  fetchNext(cb) {
    if (cb) {
      this.cb = cb;  
    }

    if (this.build.logCollection.requestOffset === -1) {
      return;
    }
    
    this.build.logCollection
      .fetchNext()
      .done(this._triggerUpdate.bind(this));
  }

  setLogPollingState(state, cb) {
    if (cb) {
      cb();
    }

    if (this.build.logCollection) {
      this.build.logCollection.shouldPoll = state;
      // if we stopped polling and are starting up again,
      // fetch the latest log size, last offset, 
      // and start polling again
      if (state) {
        this._getLog().done(this._pollLog.bind(this));
      }
    }
  }

  _resetLogViaNavigation(position) {
    const logSize = this.build.logCollection.options.size;

    const logPromise = this.build.logCollection.updateLogForNavigationChange({
      size: logSize,
      position: position
    }).fetch();
    
    logPromise.done(() => {
      this._triggerUpdate({positionChange: position});
    });
  }

  _fetchBuild() {
    const buildPromise = this.build.model.fetch();    
    
    buildPromise.fail((jqXHR) => {
        this._buildError(`Error retrieving build #${this.params.buildNumber}. See your console for more detail.`);
        console.warn(jqXHR);
      });
      
      return buildPromise;
  }

  _fetchLog() {
    if (!this.build.logCollection) {
      return;
    }

    const logPromise = this.build.logCollection.fetch();

    logPromise
      .fail((jqXHR) => {
        this._buildError(`Error retrieving log for build #${this.params.buildNumber}. See your console for more detail.`);
        console.warn(jqXHR);
      });

    return logPromise;
  }
  
  _pollLog() {
    if (!this.build.logCollection) {
      return;
    }
  
    if (this.build.logCollection.shouldPoll && this.build.model.data.state === BuildStates.IN_PROGRESS) {
      this._fetchLog().done((data, textStatus, jqxhr) => {
        this._triggerUpdate();
        this.build.logCollection.requestOffset = data.nextOffset;
        
        setTimeout(() => {
          this._pollLog();
        }, config.activeBuildLogRefresh);
      });
    }

  }
  
  _pollBuild() {
    if (!this.build.model) {
      return;
    }

    if (this.build.model.data.state === BuildStates.IN_PROGRESS) {
      this._fetchBuild().done((data) => {
        // build has finished, get most up to date info
        if (this.build.model.data.state !== BuildStates.IN_PROGRESS) {
          this._updateLogSize()
            .then(this._fetchLog.bind(this))
            .then(this._triggerUpdate.bind(this));
        }
        setTimeout(() => {
          this._pollBuild();
        }, config.activeBuildRefresh);
      });
    }
  }
  
  // If cancelled build, fetch twice to see if the build
  // is still processing or if it is actually complete.
  _processCancelledBuild() {
    this._processFinishedBuild(() => {
      this.fetchNext();
    });
  }

  _processFinishedBuild(cb) {
    this._getLog().then(this._fetchLog.bind(this)).done((data) => {
      this._triggerUpdate();
      if (cb) {
        cb();
      }
    });
  }

  _processBuildOnDeck() {
    this._triggerUpdate();  
  }

  _processActiveBuild() { 
    this._pollLog();
    this._pollBuild();
  }

  _getBuild() {
    // first get all builds so we can find the build id
    const buildStates = new Resource({
      url: `${config.apiRoot}/branches/state?property=gitInfo`,
      type: 'GET'
    }).send();

    return buildStates.then((builds) => {
      const repoBuild = findWhere(builds.map((build) => build.gitInfo), {
        host: this.params.host,
        organization: this.params.org,
        repository: this.params.repo,
        branch: this.params.branch
      });
      
      // get branch id
      const branchIdPromise = new Resource({ 
        url: `${config.apiRoot}/branches/state?property=gitInfo&property=lastBuild.id`,
        type: 'GET'
      }).send();
      
      return branchIdPromise.then((resp) => {
        
        const branchId = findWhere(resp.map((build) => build.gitInfo), {
          host: this.params.host,
          organization: this.params.org,
          repository: this.params.repo,
          branch: this.params.branch
        }).id;

        const branchHistoryPromise = new Resource({ 
          url: `${config.apiRoot}/builds/history/branch/${branchId}`,
          type: 'GET'
        }).send();
        
        return branchHistoryPromise.then((resp) => {
          
          const repoBuildId = findWhere(resp, {buildNumber: parseInt(this.params.buildNumber)}).id;
          
          // now get modules so we can get the moduleId by the module name
          const repoBuildModules = new Resource({
            url: `${config.apiRoot}/branches/${branchId}/modules`,
            type: 'GET'
          }).send();

          return repoBuildModules.then((modules) => {

            const repoBuildModule = findWhere(modules, {name: this.params.moduleName, active: true});

            // last get module build based on module id
            const buildModules = new Resource({
              url: `${config.apiRoot}/branches/builds/${repoBuildId}/modules`,
              type: 'GET'
            }).send();

            return buildModules.then((modules) => {
              const moduleBuild = findWhere(modules, {moduleId: repoBuildModule.id});

              this.build.model = new Build({
                id: moduleBuild.id,
                repoBuildId: moduleBuild.repoBuildId
              });

              return this._fetchBuild();
            });

          });

        }, (error) => {
          this.cb(error);
        });
        
      }, (error) => {
        this.cb(error);
      });

    });  

  }

  _getLog() {
    const logSizePromise = this._getLogSize();
    
    if (!logSizePromise || !this.build.model) {
      return;
    }

    logSizePromise.done((resp) => {
      this.build.logCollection = new Log({
        buildId: this.build.model.data.id,
        size: resp.size,
        buildState: this.build.model.data.state
      });    
    });
      
    return logSizePromise;
  }
  
  _getLogSize() {
    if (!this.build.model) {
      return;
    }
  
    if (buildIsOnDeck(this.build.model.data.state)) {
      return;
    }

    const logSize = new LogSize({
      buildId: this.build.model.data.id
    });

    const sizePromise = logSize.fetch();

    sizePromise.fail((err) => {
      console.warn('Error requesting log size. ', err);
      this._buildError('Error accessing build log, or log does not exist. View console for more detail');
    });

    return sizePromise;
  }
  
  _updateLogSize() {
    const logSizePromise = this._getLogSize();
    
    if (!logSizePromise) {
      return;
    }

    logSizePromise.done((resp) => {
      this.build.logCollection.options.size = resp.size;
    });
      
    return logSizePromise;
  }

  _assignBuildProcessing() {
    const buildState = this.build.model.data.state;

    switch (buildState) {
      case BuildStates.WAITING_FOR_UPSTREAM_BUILD:
      case BuildStates.WAITING_FOR_BUILD_SLOT:
      case BuildStates.QUEUED:
      case BuildStates.LAUNCHING:
        this._processBuildOnDeck();
        break;
    
      case BuildStates.IN_PROGRESS:
        this._processActiveBuild();
        break;
    
      case BuildStates.SUCCEEDED:
      case BuildStates.FAILED: 
      case BuildStates.UNSTABLE:
        this._processFinishedBuild();
        break;
      
      case BuildStates.CANCELLED:
        this._processCancelledBuild();
        break;
    }

  }

  _triggerUpdate(additional = {}) {
    const buildInfo = {
      build: this.build.model.data,
      log: this.build.logCollection || {},
      loading: false,
      positionChange: false
    };

    const buildUpdate = extend(buildInfo, additional);

    this.cb(false, buildUpdate);
  }

}

export default BuildApi;
