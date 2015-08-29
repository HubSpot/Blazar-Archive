import Reflux from 'reflux';
import BuildsActions from '../actions/buildsActions';

const BuildsStore = Reflux.createStore({

  init() {
    this.builds = [];

    this.listenTo(BuildsActions.loadBuilds, this.loadBuilds);
    this.listenTo(BuildsActions.loadBuildsSuccess, this.loadBuildsSuccess);
    this.listenTo(BuildsActions.loadBuildsError, this.loadBuildsError);
  },

  loadBuilds() {
    this.trigger({
      loading: true
    });
  },

  loadBuildsSuccess(builds) {
    this.builds = builds;

    this.trigger({
      builds: this.builds,
      loading: false
    });
  },

  loadBuildsError(error) {
    this.trigger({
      error: error,
      loading: false
    });
  }

});

export default BuildsStore;
