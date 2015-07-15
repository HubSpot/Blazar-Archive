import Reflux from 'reflux';
import BuildActions from '../actions/buildActions';

var BuildStore = Reflux.createStore({

  init() {
    this.build = {};

    this.listenTo(BuildActions.loadBuild, this.loadBuild);
    this.listenTo(BuildActions.loadBuildSuccess, this.loadBuildSuccess);
    this.listenTo(BuildActions.loadBuildError, this.loadBuildError);
  },

  loadBuild() {
    this.trigger({
      loading: true
    });
  },

  loadBuildSuccess(build) {
    this.build = build;

    this.trigger({
      build : this.build,
      loading: false
    });
  },

  loadBuildError(error) {
    this.trigger({
      error : error,
      loading: false
    });
  }

});

export default BuildStore;
