import Reflux from 'reflux';
import NewBranchActions from '../actions/newBranchActions';
import NewBranchApi from '../data/NewBranchApi';

const NewBranchStore = Reflux.createStore({

  listenables: NewBranchActions,

  init() {  
    this.branchBuildHistory = [];
    this.shouldPoll = true;
  },

  onLoadBranchBuildHistory(params) {
    this.params = params;

    NewBranchApi.fetchBranchBuildHistory(params, (resp) => {
      this.branchBuildHistory = resp;

      this.trigger({
        builds: this.branchBuildHistory,
        loadingBranches: false
      });
    });
  },

  onLoadBranchInfo(params) {
    this.params = params;

    NewBranchApi.fetchBranchInfo(params, (resp) => {
      this.branchInfo = resp;

      this.trigger({
        branchInfo: this.branchInfo
      });
    });
  },

  onLoadBranchModules(params) {
    this.params = params;

    NewBranchApi.fetchBranchModules(params, (resp) => {
      this.modules = resp;

      this.trigger({
        modules: this.modules,
        loadingModules: false
      });
    });
  },

  onLoadBranchMalformedFiles(params) {
    this.params = params;

    NewBranchApi.fetchBranchMalformedFiles(params, (resp) => {
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

  _poll() {
    this.onLoadBranchBuildHistory(this.params);

    if (this.shouldPoll) {
      setTimeout(this._poll, 5000); // TODO: replace with config var
    }
  }

});

export default NewBranchStore;
