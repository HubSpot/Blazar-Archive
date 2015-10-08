import Reflux from 'reflux';
import HostsActions from '../actions/hostsActions';

import BuildsStore from './buildsStore';
import Builds from '../collections/Builds';


const HostsStore = Reflux.createStore({

  listenables: HostsActions,

  init() {
    this.hosts = [];
    this.listenTo(BuildsStore, this.getHosts);
    this.componentListening = false;
  },

  loadBranches() {
    this.trigger({
      loading: true
    });
  },

  setListenStatus(state) {
    this.componentListening = state;
  },

  getHosts() {
    this.trigger({
      loading: true
    });

    if (!BuildsStore.buildsHaveLoaded || !this.componentListening) {
      return;
    }

    const builds = new Builds();
    builds.set(BuildsStore.getBuilds());
    this.hosts = builds.getHosts();

    this.trigger({
      hosts: this.hosts,
      loadingHosts: false
    });
  }

});

export default HostsStore;
