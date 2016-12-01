//
// Used for sidebar
//
import Reflux from 'reflux';
import BuildsActions from '../actions/buildsActions';
import BuildsApi from '../data/BuildsApi';
import {isEmpty} from 'underscore';

import reduxStore from '../reduxStore';

// since we are polling, handle errors more gracefully by only reporting
// them if we have failed multiple times
const NUMBER_OF_ALLOWED_RETRIES = 1;

const BuildsStore = Reflux.createStore({

  listenables: BuildsActions,

  init() {
    this.builds = {
      all: [],
      building: [],
      starred: []
    };
    this.failedAttempts = 0;
  },

  getBuilds() {
    return this.builds;
  },

  updateStarredBuilds(starredBranchIds) {
    this.builds.starred = this.builds.all.filter((build) => starredBranchIds.has(build.gitInfo.id));
    this.trigger({builds: this.builds});
  },

  handleFetchBuildsError(err) {
    this.failedAttempts++;
    const hasPreviouslyFetchedData = this.builds.all.length > 0;
    const exhaustedAllRetries = this.failedAttempts > NUMBER_OF_ALLOWED_RETRIES;

    if (!hasPreviouslyFetchedData || exhaustedAllRetries) {
      this.trigger({
        loading: false,
        error: {
          status: err.status,
          statusText: err.statusText
        }
      });
    }
  },

  onStopPollingBuilds() {
    BuildsApi.stopPolling();
  },

  onLoadBuilds(params) {
    const extraData = isEmpty(params);
    BuildsApi.fetchBuilds(extraData, (err, resp) => {
      if (err) {
        this.handleFetchBuildsError(err);
        return;
      }

      this.builds = resp;
      this.failedAttempts = 0;
      this.trigger({
        builds: this.builds,
        loading: false,
        error: null
      });
    });
  }

});

let previouslyStarredBranches = null;

reduxStore.subscribe(() => {
  const starredBranches = reduxStore.getState().starredBranches;
  if (previouslyStarredBranches !== starredBranches) {
    BuildsStore.updateStarredBuilds(starredBranches);
    previouslyStarredBranches = starredBranches;
  }
});

export default BuildsStore;
