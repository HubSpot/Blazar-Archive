import Reflux from 'reflux';
import OrgActions from '../actions/orgActions';

import BuildsStore from './buildsStore';
import Builds from '../collections/Builds';


const orgStore = Reflux.createStore({

  init() {
    this.repos = [];

    this.listenTo(OrgActions.setParams, this.setParams);
    this.listenTo(BuildsStore, this.getRepos);
  },

  setParams(params) {
    this.params = params;
    if (BuildsStore.buildsHaveLoaded) {
      this.getRepos();
    }
  },

  getRepos() {
    this.trigger({
      loading: true
    });

    if (!BuildsStore.buildsHaveLoaded || this.params === undefined) {
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
