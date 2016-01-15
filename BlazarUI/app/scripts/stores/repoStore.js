import Reflux from 'reflux';
import RepoActions from '../actions/repoActions';
import BranchesApi from '../data/BranchesApi';

const RepoStore = Reflux.createStore({

  listenables: RepoActions,

  onLoadBranches(params) {

    this.branchesApi = new BranchesApi({params});

    this.branchesApi.fetchBuilds((branches) => {      
      this.trigger({
        branches: branches,
        loading: false
      });
    });

  },
  
  onStopPolling() {
    if (this.branchesApi) {
      this.branchesApi.stopPollingBuilds();  
    }
  }

});

export default RepoStore;
