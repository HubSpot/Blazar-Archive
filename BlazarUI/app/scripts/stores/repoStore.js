/* global config*/
import Reflux from 'reflux';
import RepoActions from '../actions/repoActions';
import RepoApi from '../data/RepoApi';
import BranchesApi from '../data/BranchesApi';

const RepoStore = Reflux.createStore({

  listenables: RepoActions,

  init() {
    this.branchesList = [];
  },

  onLoadBranches(repoId) {
    this.repoId = repoId;

    RepoApi.fetchBranchesInRepo(repoId, (resp) => {
      this.branchesList = resp;

      this.trigger({
        branchesList: this.branchesList,
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
