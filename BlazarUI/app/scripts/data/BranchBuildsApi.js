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

  getModuleForBranch(branchId, cb) {
    // last get module build based on module id
    const buildModules = new Resource({
      url: `${config.apiRoot}/branches/${branchId}/modules`,
      type: 'GET'
    }).send();

    buildModules.then((resp) => {
      cb(resp);
    });
  }

  generateBuildModuleJsonBody(moduleIds) {
    let body = {moduleIds: moduleIds, buildDownstreams: 'NONE'};
    return body;
  }

  triggerBuildModuleSpecific(moduleIds, cb) {
    const buildPromise = new Resource({
      url: `${config.apiRoot}/branches/builds/branch/${this.branchId}`,
      type: 'POST',
      data: this.generateBuildModuleJsonBody(moduleIds)
    }).send();

    buildPromise.then((resp) => {
      this._fetchBuildHistory();
      cb(false, resp);
    }, (error) => {
      console.warn(error);
      cb('Error triggering build. Check your console for more detail.');
    });
  }
  
  triggerBuild(cb) {
    const buildPromise = new Resource({
      url: `${config.apiRoot}/branches/builds/branch/${this.branchId}`,
      type: 'POST'
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
