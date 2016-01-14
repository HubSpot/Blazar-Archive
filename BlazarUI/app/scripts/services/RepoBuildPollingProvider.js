/*global config*/
import { fromJS } from 'immutable';
import {has, findWhere, some, contains, extend, flatten} from 'underscore';
import {timestampDuration} from '../components/Helpers';
import humanizeDuration from 'humanize-duration';
import $ from 'jquery';
import Q from 'q';
import Resource from './ResourceProvider';
import ActiveBuildStates from '../constants/ActiveBuildStates';

class RepoBuildPollingProvider {

  constructor(params) {
    this.shouldPoll = true;
    this.params = params;
    this.branchId = undefined;
    
    this.promises = {
      branchId: new Resource({ url: `${config.apiRoot}/branches/state`}).get(),
      moduleBuilds: function() {
        return new Resource({url: `${config.apiRoot}/branches/builds/${this.repoBuildId}/modules`}).get();
      },      
      repoBuild: function() {
        return new Resource({ url: `${config.apiRoot}/builds/history/branch/${this.branchId}/build/${this.params.buildNumber}`}).get()
      },
      branchHistory: function() {
        return new Resource({ url: `${config.apiRoot}/builds/history/branch/${this.branchId}`}).get()
      },
      moduleNames: function() {
        return new Resource({url: `${config.apiRoot}/branches/${this.branchId}/modules`}).get();
      }
    };
  }
  
  _parseModules(modules) {
    const {params} = this;

    const modules = modules.map((module) => {
      module.blazarPath = `${config.appRoot}/builds/${params.host}/${params.org}/${params.repo}/${params.branch}/${params.buildNumber}/${module.name}`;    
      return module;
    });

    return fromJS(modules);
  }
  
  _shouldPoll(moduleBuildStates) {
    return some(moduleBuildStates, (state) => {
      return contains(ActiveBuildStates, state);
    });
  }
  
  _fetchBuilds(cb) {
    // load module builds from repoBuildId
    let promises = [];

    // On first fetch, find the branchId
    if (!this.branchId) {

      this.promises.branchId.then((builds) => {
        
        // get the branch id:
        this.branchId = findWhere(builds.map((build) => build.gitInfo), {
          host: this.params.host,
          organization: this.params.org,
          repository: this.params.repo,
          branch: this.params.branch
        }).id
        
        // get repositoryId
        this.promises.branchHistory.call(this).then((resp) => {
          this.repoBuildId = findWhere(resp, {buildNumber: parseInt(this.params.buildNumber)}).id;

          // get repo build and module build info
          Q.spread([this.promises.moduleNames.call(this), this.promises.moduleBuilds.call(this), this.promises.repoBuild.call(this)], 
            (moduleNames, moduleBuilds, repoBuild) => {

              repoBuild.duration = timestampDuration(repoBuild.startTimestamp, repoBuild.endTimestamp);
              
              // merge module names with module builds
              const moduleNamesOnly = moduleNames.map((module) => {
                return { id: module.id, name: module.name };
              });
              
              const ModuleBuildsWithName = moduleBuilds.map((build) => {
                return extend(build, findWhere(moduleNamesOnly, { id: build.moduleId }));
              });
              
              cb(false, {
                moduleBuilds: this._parseModules(ModuleBuildsWithName),
                currentRepoBuild: fromJS(repoBuild),
                branchId: this.branchId
              });
            
          }).fail((error) => {
            cb(error, null);
          }); 
          
          
        }, (error) => {
          console.warn(error);
          cb(err, null);
        });
        
      }, (error) => {
        cb(error, null);
      });
      
    }

  }

  poll(cb) {
    if (!this.shouldPoll) {
      return;
    }

    this._fetchBuilds(cb);    
  }
  
  disconnect() {
    this.shouldPoll = false;
  }  
  
}

export default RepoBuildPollingProvider;
