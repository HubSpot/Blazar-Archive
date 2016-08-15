import Reflux from 'reflux';
import OrgActions from '../actions/orgActions';
import OrgApi from '../data/OrgApi';

const RepoStore = Reflux.createStore({

  listenables: OrgActions,

  onLoadRepos(params) {
    this.orgApi = new OrgApi({params}).fetchBuilds((repos) => {
      this.trigger({
        repos,
        loading: false
      });
    });
  },

  onStopPolling() {
    if (this.branchesApi) {
      this.branchesApi.stopPollingBuilds();
    }
  }

});

export default RepoStore;
