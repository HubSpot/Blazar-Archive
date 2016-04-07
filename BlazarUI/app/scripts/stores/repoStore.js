/*global config*/
import Reflux from 'reflux';
import RepoActions from '../actions/repoActions';
import RepoApi from '../data/RepoApi';

const RepoStore = Reflux.createStore({

  listenables: RepoActions,

  init() {  
    this.branches = [];
  },

  onLoadBranches(params) {
    this.params = params;

    RepoApi.fetchBranchesInRepo(params, (resp) => {
      this.branches = resp;

      this.trigger({
        branches: this.branches,
        loading: false
      });
    });
  }

});

export default RepoStore;
