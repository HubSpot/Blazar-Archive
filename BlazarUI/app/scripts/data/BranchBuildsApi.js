/*global config*/
import { fromJS } from 'immutable';
import { findWhere } from 'underscore';
import BuildsStore from '../stores/buildsStore';
import Resource from '../services/ResourceProvider';
import humanizeDuration from 'humanize-duration';
import StoredBuilds from '../data/StoredBuilds';

class BranchBuildsApi extends StoredBuilds {
  
  _parse(resp) {
    const {params} = this.options;

    const builds = resp.map((build) => {
      build.blazarPath = `${config.appRoot}/builds/${params.host}/${params.org}/${params.repo}/${params.branch}/${build.buildNumber}`;
      if (build.endTimestamp && build.startTimestamp) {
        build.duration = humanizeDuration(build.endTimestamp - build.startTimestamp, {round: true});   
      }
      return build;
    });
    
    return fromJS(resp);
  }
  
  _afterInitialFetch() {
    if (this.pollingHistory) {
      return;
    }
    
    this.pollingHistory = true;

    // need branchId for history endpoint
    this._getBranchId();
    // fetch all builds for branchId and keep polling for changes
    this._fetchBuildHistory();
  }
  
  _getBranchId() {
    const builds = this.builds.toJS();
    const {params} = this.options;
    
    const repoBuildGitInfo = findWhere(builds.map((build) => build.gitInfo), {
      host: params.host,
      organization: params.org,
      repository: params.repo,
      branch: params.branch
    });
    
    if (repoBuildGitInfo) {
      this.branchId = repoBuildGitInfo.id;
    }

    else {
      this.cb('No such branch found');
    }
  }

  _fetchBuildHistory() {
    const branchBuildsPromise = new Resource({
      url: `${config.apiRoot}/builds/history/branch/${this.branchId}`,
      type: 'GET'
    }).send();

    branchBuildsPromise.then((resp) => {

      if (!this.shouldPoll) {
        return;
      }
      
      this.cb(false, {
        builds: this._parse(resp),
        branchId: this.branchId
      });

      setTimeout(() => {
        this._fetchBuildHistory();
      }, config.buildsRefresh);

    }, (error) => {
      this.cb('Error fetching branch builds. Check your console for more details.');
      console.warn(error);
    });
  }

  getModuleForBranch(cb) {
    // TODO: run this when the branchId is set, not just repeating on timeout forever
    if (this.branchId === undefined) {
      setTimeout(() => {
        this.getModuleForBranch(cb);
      }, 500);

      return;
    }

    // last get module build based on module id
    const buildModules = new Resource({
      url: `${config.apiRoot}/branches/${this.branchId}/modules`,
      type: 'GET'
    }).send();

    buildModules.then((resp) => {
      cb(resp);
    });
  }

  generateBuildModuleJsonBody(moduleIds, downstreamModules) {
    console.log(moduleIds);

    let body = {moduleIds: moduleIds, buildDownstreams: downstreamModules};
    return body;
  }

  triggerBuild(moduleIds, downstreamModules, cb) {
    const buildPromise = new Resource({
      url: `${config.apiRoot}/branches/builds/branch/${this.branchId}`,
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(this.generateBuildModuleJsonBody(moduleIds, downstreamModules))
    }).send();

    buildPromise.then((resp) => {
      this._fetchBuildHistory();
      cb(false, resp);
    }, (error) => {
      console.warn(error);
      cb('Error triggering build. Check your console for more detail.');
    });
  }
}

export default BranchBuildsApi;
