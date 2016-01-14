import Reflux from 'reflux';
import RepoActions from '../actions/repoActions';
import BranchesApi from '../data/BranchesApi';
import { fromJS } from 'immutable';

const RepoStore = Reflux.createStore({

  listenables: RepoActions,

  init() {
    this.branches = [];
  },
  
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
    this.branchesApi.stopPollingBuilds();
  }

});

export default RepoStore;
