import Reflux from 'reflux';
import RepoActions from '../actions/repoActions';

import BuildsStore from './buildsStore';
import Builds from '../collections/Builds';


const RepoStore = Reflux.createStore({

  init() {
    this.branches = [];
    this.listenTo(RepoActions.setParams, this.setParams);
  },

  loadBranches() {
    this.trigger({
      loading: true
    });
  },

  setParams(params) {
    if (!params) {
      this.stopListeningTo(BuildsStore);
      return false;
    }

    this.listenTo(BuildsStore, this.getBranches);
    this.params = params;

    if (BuildsStore.buildsHaveLoaded) {
      this.getBranches();
    }
  },

  getBranches() {
    this.trigger({
      loading: true
    });

    if (!BuildsStore.buildsHaveLoaded || this.params === undefined) {
      return;
    }

    const builds = new Builds();
    builds.set(BuildsStore.getBuilds());
    this.branches = builds.getBranchesByRepo(this.params);

    this.trigger({
      branches: this.branches,
      loading: false
    });
  }

});

export default RepoStore;
