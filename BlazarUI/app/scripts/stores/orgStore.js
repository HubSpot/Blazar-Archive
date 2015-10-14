import Reflux from 'reflux';
import OrgActions from '../actions/orgActions';

import BuildsStore from './buildsStore';
import Builds from '../collections/Builds';


const orgStore = Reflux.createStore({

  init() {
    this.repos = [];
    this.listenTo(OrgActions.setParams, this.setParams);
  },

  setParams(params) {
    if (!params) {
      this.stopListeningTo(BuildsStore);
      return false;
    }

    this.params = params;
    this.listenTo(BuildsStore, this.getRepos);

    if (BuildsStore.buildsHaveLoaded) {
      this.getRepos();
    }
  },

  getRepos() {
    if (!BuildsStore.buildsHaveLoaded) {
      return;
    }

    const builds = new Builds();
    builds.set(BuildsStore.getBuilds());
    this.repos = builds.getReposByOrg(this.params);

    this.trigger({
      repos: this.repos,
      loading: false
    });
  }

});

export default orgStore;
