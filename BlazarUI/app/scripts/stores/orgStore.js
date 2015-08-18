import Reflux from 'reflux';
import OrgActions from '../actions/orgActions';

var orgStore = Reflux.createStore({

  init() {
    this.repos = [];

    this.listenTo(OrgActions.loadRepos, this.loadRepos);
    this.listenTo(OrgActions.loadReposSuccess, this.loadReposSuccess);
    this.listenTo(OrgActions.loadReposError, this.loadReposError);
  },

  loadRepos() {
    this.trigger({
      loading: true
    });
  },

  loadReposSuccess(repos) {
    this.repos = repos;

    this.trigger({
      repos : this.repos,
      loading: false
    });
  },

  loadReposError(error) {
    this.trigger({
      error : error,
      loading: false
    });
  }

});

export default orgStore;
