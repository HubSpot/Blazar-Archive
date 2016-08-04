import Reflux from 'reflux';
import HostsActions from '../actions/hostsActions';
import HostsApi from '../data/HostsApi';

const HostsStore = Reflux.createStore({

  listenables: HostsActions,

  onLoadHosts() {
    const hostsApi = new HostsApi().fetchBuilds((hosts) => {
      this.trigger({
        hosts,
        loadingHosts: false
      });
    });
  }


});

export default HostsStore;
