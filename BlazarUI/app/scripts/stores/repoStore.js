/*global config*/
import Reflux from 'reflux';
import RepoActions from '../actions/repoActions';
import RepoApi from '../data/RepoApi';
import BranchesApi from '../data/BranchesApi';

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
  },

  onLoadBranchesAndBuilds(params) {
    this.branchesApi = new BranchesApi({params});

    this.branchesApi.fetchBuilds((resp) => {
      this.trigger({
        branches: resp,
        loading: false
      });
    });
  }

});

export default RepoStore;
