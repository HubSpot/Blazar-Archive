import Reflux from 'reflux';
import HostsActions from '../actions/hostsActions';

import BuildsStore from './buildsStore';
import Builds from '../collections/Builds';


const HostsStore = Reflux.createStore({

  listenables: HostsActions,

  init() {
    this.hosts = [];
  },

  setListenStatus(state) {
    if (state) {
      this.listenTo(BuildsStore, this.getHosts);
    }
    else {
      this.stopListeningTo(BuildsStore);
    }
  },

  getHosts() {
    this.trigger({
      loadingHosts: true
    });

    if (!BuildsStore.buildsHaveLoaded) {
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
