import Reflux from 'reflux';
import BranchActions from '../actions/branchActions';

import BuildsStore from './buildsStore';
import Builds from '../collections/Builds';

const BranchStore = Reflux.createStore({

  init() {
    this.modules = [];
    this.listenTo(BranchActions.setParams, this.setParams);
  },

  setParams(params) {
    if (!params) {
      this.stopListeningTo(BuildsStore);
      return false;
    }

    this.params = params;
    this.listenTo(BuildsStore, this.getModules);

    if (BuildsStore.buildsHaveLoaded) {
      this.getModules();
    }
  },

  getModules() {
    if (!BuildsStore.buildsHaveLoaded) {
      return;
    }

    this.trigger({
      loading: true
    });

    const builds = new Builds();
    builds.set(BuildsStore.getBuilds());

    const branch = builds.getBranchModules(this.params);
    this.modules = branch.modules || [];

    this.trigger({
      modules: this.modules,
      loading: false
    });
  }

});

export default BranchStore;
