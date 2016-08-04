import Reflux from 'reflux';
import BranchActions from '../actions/branchActions';
import BranchApi from '../data/BranchApi';

const BranchStore = Reflux.createStore({

  listenables: BranchActions,

  init() {
    this.branchBuildHistory = [];
    this.shouldPoll = true;
    this.isRequestingBuildHistory = false;
  },

  onLoadBranchBuildHistory(params) {
    this.params = params;

    if (this.isRequestingBuildHistory) {
      return;
    }

    this.isRequestingBuildHistory = BranchApi.fetchBranchBuildHistory(params, (resp) => {
      this.branchBuildHistory = resp;
      this.isRequestingBuildHistory = false;

      this.trigger({
        builds: this.branchBuildHistory,
        loadingBranches: false
      });
    });
  },

  onLoadBranchInfo(params) {
    this.params = params;

    BranchApi.fetchBranchInfo(params, (resp) => {
      this.branchInfo = resp;

      this.trigger({
        branchInfo: this.branchInfo,
        loadingBranchInfo: false
      });
    });
  },

  onLoadBranchModules(params) {
    this.params = params;

    BranchApi.fetchBranchModules(params, (resp) => {
      this.modules = resp;

      this.trigger({
        modules: this.modules,
        loadingModules: false
      });
    });
  },

  onLoadMalformedFiles(params) {
    this.params = params;

    BranchApi.fetchMalformedFiles(params, (resp) => {
      this.malformedFiles = resp;

      this.trigger({
        malformedFiles: this.malformedFiles,
        loadingMalformedFiles: false
      });
    });
  },

  onStartPolling(params) {
    this.params = params;
    this.shouldPoll = true;
    this._poll();
  },

  onStopPolling() {
    this.shouldPoll = false;
  },

  onTriggerBuild(params, state) {
    const {selectedModules, buildDownstreamModules, resetCache} = state;

    BranchApi.triggerBuild(params, selectedModules, buildDownstreamModules, resetCache, (error) => {
      if (error) {
        this.error = error;
        this.triggerErrorUpdate();
        return;
      }

      this.onLoadBranchBuildHistory(params);
    });
  },

  triggerErrorUpdate() {
    this.trigger({
      error: this.error,
      loadingBranches: false
    });

    this.error = undefined;
  },

  _poll() {
    this.onLoadBranchBuildHistory(this.params);

    if (this.shouldPoll) {
      setTimeout(this._poll, window.config.buildsRefresh);
    }
  }

});

export default BranchStore;
