import Reflux from 'reflux';
import BuildHistoryActions from '../actions/buildHistoryActions';

const buildHistoryStore = Reflux.createStore({

  listenables: BuildHistoryActions,

  init() {
    this.buildHistory = [];
    this.modulesBuildHistory = [];
  },

  loadBuildHistory() {
    this.trigger({
      loadingHistory: true
    });
  },

  loadBuildHistorySuccess(buildHistory) {
    this.buildHistory = buildHistory;

    this.trigger({
      buildHistory: this.buildHistory,
      loadingHistory: false
    });
  },

  loadModulesBuildHistorySuccess(buildHistory) {
    this.modulesBuildHistory = buildHistory;

    this.modulesBuildHistory.sort((a, b) => {
      if (a.module.moduleName < b.module.moduleName) {
        return -1;
      }
      if (a.module.moduleName > b.module.moduleName) {
        return 1;
      }
      return 0;
    });

    this.trigger({
      modulesBuildHistory: this.modulesBuildHistory,
      loadingModulesBuildHistory: false
    });
  },

  loadBuildHistoryError(error) {
    this.trigger({
      error: error,
      loadingHistory: false
    });
  }

});

export default buildHistoryStore;
