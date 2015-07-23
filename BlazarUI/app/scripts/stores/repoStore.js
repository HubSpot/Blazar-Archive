import Reflux from 'reflux';
import RepoActions from '../actions/repoActions';

var RepoStore = Reflux.createStore({

  init() {
    this.branches = [];

    this.listenTo(RepoActions.loadBranches, this.loadBranches);
    this.listenTo(RepoActions.loadBranchesSuccess, this.loadBranchesSuccess);
    this.listenTo(RepoActions.loadBranchesError, this.loadBranchesError);
  },

  loadBranches() {
    this.trigger({
      loading: true
    });
  },

  loadBranchesSuccess(branches) {
    this.branches = branches;

    this.trigger({
      branches : this.branches,
      loading: false
    });
  },

  loadBranchesError(error) {
    this.trigger({
      error : error,
      loading: false
    });
  }

});

export default RepoStore;
