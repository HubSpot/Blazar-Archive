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
      branchId: new Resource({
        url: `${config.apiRoot}/branches/state`,
        type: 'GET'
      }).send(),
      
      moduleBuilds: function() {
        return new Resource({
          url: `${config.apiRoot}/branches/builds/${this.repoBuildId}/modules`,
          type: 'GET'
        }).send();
      },      
      repoBuild: function() {
        return new Resource({
          url: `${config.apiRoot}/builds/history/branch/${this.branchId}/build/${this.params.buildNumber}`,
          type: 'GET'
        }).send();
      },
      branchHistory: function() {
        return new Resource({
          url: `${config.apiRoot}/builds/history/branch/${this.branchId}`,
          tpe: 'GET'
        }).send();
      },
      moduleNames: function() {
        return new Resource({
          url: `${config.apiRoot}/branches/${this.branchId}/modules`,
          type: 'GET'
        }).send();
      }
    };
  }
  
  _parseModules(modules) {
    const {params} = this;

    const modules = modules.map((module) => {
      module.blazarPath = `/builds/${params.host}/${params.org}/${params.repo}/${params.branch}/${params.buildNumber}/${module.name}`.replace('#', '%23');    
      return module;
    });

    return fromJS(modules);
  }
  
  _shouldPoll(moduleBuildStates) {
    if (moduleBuildStates.length === 0) {
      return true;
    }
  
    return some(moduleBuildStates, (state) => {
      return contains(ActiveBuildStates, state);
    });
  }

  _fetchBuilds(cb) {

    this.promises.branchId.then((builds) => {
      
      // get the branch id:
      this.branchId = findWhere(builds.map((build) => build.gitInfo), {
        host: this.params.host,
        organization: this.params.org,
        repository: this.params.repo,
        branch: this.params.branch
      }).id;
      
      // get repositoryId
      this.promises.branchHistory.call(this).then((resp) => {
        this.repoBuildId = findWhere(resp, {buildNumber: parseInt(this.params.buildNumber)}).id;

        // get repo build and module build info
        Q.spread([this.promises.moduleNames.call(this), this.promises.moduleBuilds.call(this), this.promises.repoBuild.call(this)], 
          (moduleNames, moduleBuilds, repoBuild) => {

            const moduleBuildStates = flatten(moduleBuilds.map((build) => {
              return build.state;
            }));

            // stop polling if build is in a terminal state
            if (!this._shouldPoll(moduleBuildStates)) {
              this.disconnect();
            }
            
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

            setTimeout(() => {
              this.poll(cb);
            }, config.activeBuildModuleRefresh);
          
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
