import Reflux from 'reflux';
import BuildHistoryActions from '../actions/buildHistoryActions';

const buildHistoryStore = Reflux.createStore({

  init() {
    this.buildHistory = [];
    this.modulesBuildHistory = [];

    this.buildTriggeringDone = false;
    this.buildTriggeringError = '';

    this.listenTo(BuildHistoryActions.loadBuildHistory, this.loadBuildHistory);
    this.listenTo(BuildHistoryActions.loadBuildHistorySuccess, this.loadBuildHistorySuccess);
    this.listenTo(BuildHistoryActions.loadModulesBuildHistorySuccess, this.loadModulesBuildHistorySuccess);
    this.listenTo(BuildHistoryActions.loadBuildHistoryError, this.loadBuildHistoryError);

    this.listenTo(BuildHistoryActions.triggerBuildSuccess, this.triggerBuildSuccess);
    this.listenTo(BuildHistoryActions.triggerBuildError, this.triggerBuildError);
    this.listenTo(BuildHistoryActions.triggerBuildStart, this.triggerBuildStart);
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
      loading: false
    });
  },

  triggerBuildStart() {
    this.trigger({
      buildTriggeringDone: false
    });
  },

  triggerBuildSuccess() {
    this.trigger({
      buildTriggeringDone: true
    });
  },

  triggerBuildError(error) {
    this.trigger({
      buildTriggeringDone: true,
      buildTriggeringError: error
    });
  }

});

export default buildHistoryStore;
