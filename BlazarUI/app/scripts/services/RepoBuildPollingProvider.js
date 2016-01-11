/*global config*/
import { fromJS } from 'immutable';
import {has, findWhere, some, contains} from 'underscore';
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
    
    this.repoBuildPromise = new Resource({url: `${config.apiRoot}/branches/builds/${params.repoBuildId}/modules`});
    this.branchIdPromise = new Resource({ url: `${config.apiRoot}/branches/state`}).get();
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

  poll(cb) {
    if (!this.shouldPoll) {
      return;
    }

    // load module builds from repoBuildId
    let promises = [this.repoBuildPromise.get()];

    // On first fetch, find branchId based on params so we can load the starred state
    if (!this.branchId) {
      promises.push(this.branchIdPromise);
    }

    Q.spread(promises, (moduleBuilds, allBuilds) => {      
      
      if (allBuilds) {
        this._getBranchId(allBuilds);
      }
      
      cb(false, {
        moduleBuilds: fromJS(moduleBuilds),
        branchId: this.branchId  
      });
    
      // check if we need to keep polling
      const moduleBuildStates = moduleBuilds.map((build) => build.state);
      
      const shouldPoll = some(moduleBuildStates, (state) => {
        return contains(ActiveBuildStates, state);
      });
      
      if (!shouldPoll) {
        return;
      }
      
      setTimeout(() => {
        this.poll.call(this, cb);
      }, config.buildsRefresh);

    })
    .fail((xhr) => {
      cb(xhr, null);  
    });
    
  }
  
  disconnect() {
    this.shouldPoll = false;
  }  
  
}

export default RepoBuildPollingProvider;
