import Reflux from 'reflux';
import BuildsActions from '../actions/buildsActions';
import {updateBuilds} from '../utils/buildsHelpers';

const BuildsStore = Reflux.createStore({

  listenables: BuildsActions,

  init() {
    this.builds = [];
    this.buildsLoaded = false;
    this.buildsHaveLoaded = false;
  },

  getBuilds() {
    return this.builds;
  },

  loadBuilds() {
    this.trigger({
      loadingBuilds: true
    });
  },

  loadBuildsSuccess(incomingBuilds) {
    // initial fetch
    if (!this.buildsHaveLoaded) {
      this.builds = incomingBuilds;
    }
    // subsequent fetches
    else {
      const updatedBuilds = updateBuilds(incomingBuilds, this.builds);
      this.builds = updatedBuilds;
    }

    this.trigger({
      builds: this.builds,
      loadingBuilds: false
    });

    this.buildsHaveLoaded = true;
  },

  loadBuildsError(error) {
    this.trigger({
      error: error,
      loadingBuilds: false
    });
  }

});

export default BuildsStore;
