import Reflux from 'reflux';
import BranchActions from '../actions/branchActions';
import BranchBuildsApi from '../data/BranchBuildsApi';
import Immutable from 'immutable';

const BranchStore = Reflux.createStore({

  listenables: BranchActions,

  onStopPolling() {
    if (this.branchBuildsApi) {
      this.branchBuildsApi.stopPollingBuilds();
      this.branchBuildsApi = undefined;  
    }
  },

  onLoadModules() {
    this.branchBuildsApi.getModuleForBranch((resp) => {
      this.triggerModuleUpdate(resp);
    });
  },

  onLoadMalformedFiles() {
    this.branchBuildsApi.getMalformedFiles((resp) => {
      this.triggerMalformedFileUpdate(resp);
    });
  },

  onLoadBranchBuilds(params) {
    this.branchBuildsApi = new BranchBuildsApi({
      params: params
    });

    this.branchBuildsApi.fetchBuilds((error, resp) => {
      if (error) {
        this.error = error;
        return this.triggerErrorUpdate();
      }

      this.data = resp;
      this.triggerUpdate();
    });
  },

  onTriggerBuild(moduleIds, downstreamModules) {
    this.branchBuildsApi.triggerBuild(moduleIds, downstreamModules, (error, resp) => {
      if (error) {
        this.error = error;
        return this.triggerErrorUpdate();
      }
    });
  },

  triggerUpdate() {
    this.trigger({
      builds: this.data.builds,
      branchId: this.data.branchId,
      loadingBranches: false  
    });
  },

  triggerModuleUpdate(resp) {
    this.trigger({
      modules: Immutable.fromJS(resp),
      loadingModules: false
    });
  },

  triggerMalformedFileUpdate(resp) {
    this.trigger({
      malformedFiles: resp,
      loadingMalformedFiles: false
    });
  },

  triggerErrorUpdate() {
    this.trigger({
      error: this.error,
      loadingBranches: false
    });

    this.error = undefined;
  }

});
  
export default BranchStore;
