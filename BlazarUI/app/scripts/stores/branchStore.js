import Reflux from 'reflux';
import BranchActions from '../actions/branchActions';

import BuildsStore from './buildsStore';
import Builds from '../collections/Builds';

const BranchStore = Reflux.createStore({

  init() {
    this.modules = [];

    this.listenTo(BranchActions.setParams, this.setParams);
    this.listenTo(BuildsStore, this.getModules);
  },

  setParams(params) {
    this.params = params;
    if (BuildsStore.buildsHaveLoaded) {
      this.getModules();
    }
  },

  getModules() {
    this.trigger({
      loading: true
    });

    if (!BuildsStore.buildsHaveLoaded || this.params === undefined) {
      return;
    }

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
