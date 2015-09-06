import Reflux from 'reflux';
import BuildHistoryActions from '../actions/buildHistoryActions';

const buildHistoryStore = Reflux.createStore({

  init() {
    this.buildHistory = [];
    this.modulesBuildHistory = [];

    this.listenTo(BuildHistoryActions.loadBuildHistory, this.loadBuildHistory);
    this.listenTo(BuildHistoryActions.loadBuildHistorySuccess, this.loadBuildHistorySuccess);
    this.listenTo(BuildHistoryActions.loadModulesBuildHistorySuccess, this.loadModulesBuildHistorySuccess);
    this.listenTo(BuildHistoryActions.loadBuildHistoryError, this.loadBuildHistoryError);
  },

  loadBuildHistory() {
    this.trigger({
      loading: true
    });
  },

  loadBuildHistorySuccess(buildHistory) {
    this.buildHistory = buildHistory;

    this.trigger({
      buildHistory: this.buildHistory,
      loading: false
    });
  },


  loadModulesBuildHistorySuccess(buildHistory) {
    this.modulesBuildHistory = buildHistory;
    
    this.trigger({
      modulesBuildHistory: this.modulesBuildHistory,
      loadingModulesBuildHistory: false
    });
  },


  loadBuildHistoryError(error) {
    this.trigger({
      error: error,
      loading: false
    });
  }

});

export default buildHistoryStore;
