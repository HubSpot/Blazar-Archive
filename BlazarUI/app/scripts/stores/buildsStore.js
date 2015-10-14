import Reflux from 'reflux';
import BuildsActions from '../actions/buildsActions';
import {updateBuilds, sortBuilds} from '../utils/buildsHelpers';

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
      this.builds = sortBuilds(incomingBuilds);
    }
    // subsequent fetches
    else {
      const updatedBuilds = sortBuilds(updateBuilds(incomingBuilds, this.builds));
      this.builds = updatedBuilds;
    }

    this.buildsHaveLoaded = true;
    
    this.trigger({
      builds: this.builds,
      loadingBuilds: false
    });

  },

  loadBuildsError(error) {
    this.trigger({
      error: error,
      loadingBuilds: false
    });
  }

});

export default BuildsStore;
