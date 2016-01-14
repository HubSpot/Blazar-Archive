import Reflux from 'reflux';
import BranchActions from '../actions/branchActions';
import BranchBuildsApi from '../data/BranchBuildsApi';

const BranchStore = Reflux.createStore({

  listenables: BranchActions,

  onStopPolling() {
    this.branchBuildsApi.stopPollingBuilds();
  },

  onLoadBranchBuilds(params) {
    this.branchBuildsApi = new BranchBuildsApi({
      params: params
    });

    this.branchBuildsApi.fetchBuilds((err, resp) => {
      if (err) {
        return this.trigger({
          error: err,
          loading: false
        });
      }

      this.trigger({
        builds: resp,
        loading: false
      });
      
    });
  },

});
  
export default BranchStore;
