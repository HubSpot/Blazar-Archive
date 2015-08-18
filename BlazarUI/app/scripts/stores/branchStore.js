import Reflux from 'reflux';
import BranchActions from '../actions/branchActions';

var BranchStore = Reflux.createStore({

  init() {
    this.modules = [];

    this.listenTo(BranchActions.loadModules, this.loadModules);
    this.listenTo(BranchActions.loadModulesSuccess, this.loadModulesSuccess);
    this.listenTo(BranchActions.loadModulesError, this.loadModulesError);
  },

  loadModules() {
    this.trigger({
      loading: true
    });
  },

  loadModulesSuccess(modules) {
    this.modules = modules;

    this.trigger({
      modules : this.modules,
      loading: false
    });
  },

  loadModulesError(error) {
    this.trigger({
      error : error,
      loading: false
    });
  }

});

export default BranchStore;
