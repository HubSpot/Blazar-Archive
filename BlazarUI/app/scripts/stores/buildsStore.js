import Reflux from 'reflux';
import BuildsActions from '../actions/buildsActions';

const BuildsStore = Reflux.createStore({

  init() {
    this.builds = [];
    this.buildsLoaded = false;

    this.listenTo(BuildsActions.loadBuilds, this.loadBuilds);
    this.listenTo(BuildsActions.loadBuildsSuccess, this.loadBuildsSuccess);
    this.listenTo(BuildsActions.loadBuildsError, this.loadBuildsError);
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
