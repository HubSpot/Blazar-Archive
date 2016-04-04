import Reflux from 'reflux';

import NewRepoBuildActions from '../actions/newRepoBuildActions';
import NewRepoBuildApi from '../data/NewRepoBuildApi';
import ActiveBuildStates from '../constants/ActiveBuildStates';

import { buildIsInactive } from '../components/Helpers';

const NewRepoBuildStore = Reflux.createStore({

  listenables: NewRepoBuildActions,

  init() {  
    this.repoBuild = {};
    this.moduleBuilds = [];
    this.shouldPoll = true;
  },

  onLoadRepoBuild(params) {
    this.params = params;

    NewRepoBuildApi.fetchRepoBuild(params, (resp) => {
      this.repoBuild = resp;

      if (buildIsInactive(this.repoBuild.state)) {
        this.shouldPoll = false;
      }

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
  },

  onStartPolling(params) {
    this.params = params;
    this.shouldPoll = true;
    this._poll();
  },

  onStopPolling() {
    this.shouldPoll = false;
  },

  _poll() {
    this.onLoadModuleBuilds(this.params);
    this.onLoadRepoBuild(this.params);

    if (this.shouldPoll) {
      setTimeout(this._poll, 1000); // TODO: replace with config var
    }
  }

});

export default NewRepoBuildStore;
