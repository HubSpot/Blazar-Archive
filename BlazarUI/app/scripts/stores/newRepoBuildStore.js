import Reflux from 'reflux';
import NewRepoBuildActions from '../actions/newRepoBuildActions';
import NewRepoBuildApi from '../data/NewRepoBuildApi';

const NewRepoBuildStore = Reflux.createStore({

  listenables: NewRepoBuildActions,

  init() {  
    this.repoBuild = {};
    this.moduleBuilds = [];
  },

  onLoadRepoBuild(params) {
    this.params = params;

    NewRepoBuildApi.fetchRepoBuild(params, (resp) => {
      this.repoBuild = resp;

      this.trigger({
        currentRepoBuild: this.repoBuild,
        loadingRepoBuild: false
      });
    });
  },

  onLoadModuleBuilds(params) {
    this.params = params;

    NewRepoBuildApi.fetchModuleBuilds(params, (resp) => {
      this.moduleBuilds = resp;

      this.trigger({
        moduleBuilds: this.moduleBuilds,
        loadingModuleBuilds: false
      });
    });
  }

});

export default NewRepoBuildStore;
