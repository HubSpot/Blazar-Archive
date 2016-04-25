import Reflux from 'reflux';

import RepoBuildActions from '../actions/repoBuildActions';
import RepoBuildApi from '../data/RepoBuildApi';
import ActiveBuildStates from '../constants/ActiveBuildStates';

import { buildIsInactive } from '../components/Helpers';

const RepoBuildStore = Reflux.createStore({

  listenables: RepoBuildActions,

  init() {  
    this.repoBuild = {};
    this.moduleBuilds = [];
    this.shouldPoll = true;
  },

  onLoadRepoBuild(params) {
    this.params = params;

    RepoBuildApi.fetchRepoBuild(params, (resp) => {
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

  onLoadRepoBuildById(repoBuildId) {
    RepoBuildApi.fetchRepoBuildById(repoBuildId, (resp) => {
      this.trigger({
        repoBuild: resp,
        loading: false
      });
    });
  },

  onLoadModuleBuilds(params) {
    this.params = params;

    RepoBuildApi.fetchModuleBuilds(params, (resp) => {
      this.moduleBuilds = resp;

      this.trigger({
        moduleBuilds: this.moduleBuilds,
        loadingModuleBuilds: false
      });
    });
  },

  onLoadModuleBuildsById(branchId, repoBuildId, buildNumber) {
    RepoBuildApi.fetchModuleBuildsById(branchId, repoBuildId, buildNumber, (resp) => {
      this.trigger({
        moduleBuildsList: resp,
        loading: false
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

  onCancelBuild(params) {
    RepoBuildApi.cancelBuild(params);
  },

  _poll() {
    this.onLoadModuleBuilds(this.params);
    this.onLoadRepoBuild(this.params);

    if (this.shouldPoll) {
      setTimeout(this._poll, config.activeBuildModuleRefresh);
    }
  }

});

export default RepoBuildStore;
