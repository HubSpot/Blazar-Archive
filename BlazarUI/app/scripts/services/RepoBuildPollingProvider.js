/*global config*/
import { fromJS } from 'immutable';
import {has, findWhere, some, contains, extend} from 'underscore';
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
      repoBuildPromise: new Resource({url: `${config.apiRoot}/branches/builds/${params.repoBuildId}/modules`}),
      branchIdPromise: new Resource({ url: `${config.apiRoot}/branches/state`}).get(),
      modulesPromise: function() {
        return new Resource({url: `${config.apiRoot}/branches/${this.branchId}/modules`}).get();
      }
    };
  }
  
  _getBranchId(builds) {
    const repoBuild = findWhere(builds.map((build) => build.gitInfo), {
      host: this.params.host,
      organization: this.params.org,
      repository: this.params.repo,
      branch: this.params.branch
    });
  
    this.branchId = repoBuild ? repoBuild.id : null;
  }
  
  _shouldPoll(moduleBuildStates) {
    return some(moduleBuildStates, (state) => {
      return contains(ActiveBuildStates, state);
    });
  }
  
  _fetchBuilds(cb) {
    // load module builds from repoBuildId
    let promises = [this.promises.repoBuildPromise.get()];

    // On first fetch, find branchId based on params so we can load the starred state
    if (!this.branchId) {
      promises.push(this.promises.branchIdPromise);
    }
    
    Q.spread(promises, (moduleBuilds, allBuilds) => {
      
      if (allBuilds) {
        this._getBranchId(allBuilds);
      }

      // get module names
      const modulesPromise = this.promises.modulesPromise.call(this);

      modulesPromise.then((modules) => {
        const moduleNamesOnly = modules.map((module) => {
          return { id: module.id, name: module.name };
        });
        
        // add module name to module builds
        const ModuleBuildsWithName = moduleBuilds.map((build) => {
          return extend(build, findWhere(moduleNamesOnly, { id: build.moduleId }));
        });
        
        // send module builds to store
        cb(false, {
          moduleBuilds: fromJS(ModuleBuildsWithName),
          branchId: this.branchId
        });
        
        // check if we need to keep polling
        const moduleBuildStates = moduleBuilds.map((build) => build.state);
        
        if (!this._shouldPoll(moduleBuildStates)) {
          return;
        }

        setTimeout(() => {
          this.poll.call(this, cb);
        }, config.buildsRefresh);
            
      }, (error) => {
        cb(xhr, null); 
      });
      
    })
    .fail((xhr) => {
      cb(xhr, null);
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
