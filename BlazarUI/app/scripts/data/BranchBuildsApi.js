/*global config*/
import { fromJS } from 'immutable';
import { findWhere } from 'underscore';
import BuildsStore from '../stores/buildsStore';
import Resource from '../services/ResourceProvider';
import humanizeDuration from 'humanize-duration';

class BranchBuildsApi {
  
  constructor({params}) {
    this.params = params;
    this.shouldPoll = true;
  }
  
  _parse(resp) {
    const {params} = this;
    const builds = resp.map((build) => {
      build.blazarPath = `${config.appRoot}/builds/${params.host}/${params.org}/${params.repo}/${params.branch}/${build.buildNumber}`;
      if (build.endTimestamp && build.startTimestamp) {
        build.duration = humanizeDuration(build.endTimestamp - build.startTimestamp, {round: true});   
      }
      return build;
    });
    
    return fromJS(resp);
  }

  _onStoreChange(resp) {
    this.builds = resp.builds.all;
    // need branchId for history endpoint
    this._getBranchId();
    // fetch all builds for branchId and keep polling for changes
    this._fetchBuildHistory();
    // we only needed the branchId, dont need to listen anymore
    this._unsubscribeFromBuilds();
  }
  
  _getBranchId() {
    const builds = this.builds.toJS();
    const repoBuildGitInfo = findWhere(builds.map((build) => build.gitInfo), {
      host: this.params.host,
      organization: this.params.org,
      repository: this.params.repo,
      branch: this.params.branch
    });
    
    if (repoBuildGitInfo) {
      this.branchId = repoBuildGitInfo.id;
    }

    else {
      this.cb('No such branch found');
    }
  }

  _fetchBuildHistory() {
    const branchBuildsPromise = new Resource({url: `${config.apiRoot}/builds/history/branch/${this.branchId}`}).get();

    branchBuildsPromise.then((resp) => {
      this.cb(false, this._parse(resp));

      if (this.shouldPoll) {
        setTimeout(() => {
          this._fetchBuildHistory();
        }, config.buildsRefresh);
      }
    }, (error) => {
      this.cb('Error fetching branch builds. Check your console for more details.');
      console.warn(error);
    });
  }

  //
  // Public
  //

  fetchBuilds(cb) {
    this.cb = cb;
    this._unsubscribeFromBuilds = BuildsStore.listen(this._onStoreChange.bind(this));
  }
  
  stopPolling() {
    this.shouldPoll = false;
  }
  
}

export default BranchBuildsApi;
