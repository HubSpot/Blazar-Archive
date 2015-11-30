import Reflux from 'reflux';
import RepoActions from '../actions/repoActions';


const RepoStore = Reflux.createStore({

  listenables: RepoActions,
  
  init() {
    this.branches = [];
  },

  loadBranchesSuccess(incomingData) {
    this.branches = incomingData;
    
    this.trigger({
      branches: this.branches,
      loading: false
    });
  },
  
  loadBranchesError(error) {
    this.trigger({
      error: error
    });
  }

});

export default RepoStore;
