/*global config*/
import Reflux from 'reflux';
import RepoActions from '../actions/repoActions';
import RepoApi from '../data/RepoApi';

const RepoStore = Reflux.createStore({

  listenables: RepoActions,

  init() {  
    this.branches = [];
  },

  onLoadBranches(repoId) {
    this.repoId = repoId;

    RepoApi.fetchBranchesInRepo(repoId, (resp) => {
      this.branches = resp;

      this.trigger({
        branches: this.branches,
        loadingRepo: false
      });
    });
  }

});

export default RepoStore;
