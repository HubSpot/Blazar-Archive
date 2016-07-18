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
    this.moduleBuildsList = [];
    this.isRequestingRepoBuild = false;
    this.isRequestingModuleBuilds = false;
  },

  onLoadRepoBuild(params) {
    this.params = params;

    if (this.isRequestingRepoBuild) {
      return;
    }

    this.isRequestingRepoBuild = RepoBuildApi.fetchRepoBuild(params, (resp) => {
      this.repoBuild = resp;
      this.isRequestingRepoBuild = false;

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

    if (this.isRequestingModuleBuilds) {
      return;
    }

    this.isRequestingModuleBuilds = RepoBuildApi.fetchModuleBuilds(params, (resp) => {
      this.moduleBuilds = resp;
      this.isRequestingModuleBuilds = false;

      this.trigger({
        moduleBuilds: this.moduleBuilds,
        loadingModuleBuilds: false
      });
    });
  },

  onLoadModuleBuildsById(branchId, repoBuildId, buildNumber) {
    RepoBuildApi.fetchModuleBuildsById(branchId, repoBuildId, buildNumber, (resp) => {
      this.moduleBuildsList = resp;

      this.trigger({
        moduleBuildsList: this.moduleBuildsList,
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
