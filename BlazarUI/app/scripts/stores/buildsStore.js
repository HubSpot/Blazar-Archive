import Reflux from 'reflux';
import BuildsActions from '../actions/buildsActions';

const BuildsStore = Reflux.createStore({

  listenables: BuildsActions,

  init() {
    this.builds = [];
    this.buildsLoaded = false;
  },

  getBuilds() {
    return this.builds;
  },

  haveBuildsLoaded() {
    return this.buildsLoaded;
  },

  loadBuilds() {
    this.trigger({
      loadingBuilds: true
    });
  },

  loadBuildsSuccess(builds) {
    this.builds = builds;
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
