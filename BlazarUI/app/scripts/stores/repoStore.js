/*global config*/
import Reflux from 'reflux';
import RepoActions from '../actions/repoActions';
import RepoApi from '../data/RepoApi';

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
  }
});

export default RepoStore;
