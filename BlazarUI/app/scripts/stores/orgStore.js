import Reflux from 'reflux';
import OrgActions from '../actions/orgActions';
import OrgApi from '../data/OrgApi';

const RepoStore = Reflux.createStore({

  listenables: OrgActions,

  onLoadRepos(params) {
    this.orgApi = new OrgApi({params}).fetchBuilds((repos) => {
      this.trigger({
        repos: repos,
        loading: false
      });
    });
  }

});

export default RepoStore;
