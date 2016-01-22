import Reflux from 'reflux';
import BranchActions from '../actions/branchActions';
import BranchBuildsApi from '../data/BranchBuildsApi';

const BranchStore = Reflux.createStore({

  listenables: BranchActions,

  onStopPolling() {
    if (this.branchBuildsApi) {
      this.branchBuildsApi.stopPollingBuilds();
      this.branchBuildsApi = undefined;  
    }
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
  
  onTriggerBuild() {
    this.branchBuildsApi.triggerBuild((error, resp) => {
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

  triggerErrorUpdate() {
    this.trigger({
      error: this.error,
      loadingBranches: false
    });

    this.error = undefined;
  }

});
  
export default BranchStore;
