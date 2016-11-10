import Reflux from 'reflux';

import RepoBuildActions from '../actions/repoBuildActions';
import RepoBuildApi from '../data/RepoBuildApi';

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

  onLoadRepoBuild(params, cacheRepoBuildId = false) {
    this.params = params;

    if (this.isRequestingRepoBuild) {
      return;
    }

    this.isRequestingRepoBuild = true;

    const fetchRepoBuildPromise = cacheRepoBuildId && this._isRepoBuildPreviouslyFetched() ?
      RepoBuildApi.fetchRepoBuildById(this.repoBuild.id) :
      RepoBuildApi.fetchRepoBuild(params);

    fetchRepoBuildPromise
      .then((resp) => {
        this.repoBuild = resp;
        this.isRequestingRepoBuild = false;

        if (buildIsInactive(this.repoBuild.state)) {
          this.shouldPoll = false;
        }

        this.trigger({
          currentRepoBuild: this.repoBuild,
          loadingRepoBuild: false
        });
      }, (error) => {
        this.isRequestingRepoBuild = false;
        this.shouldPoll = false;
        this.trigger({
          error,
          loadingRepoBuild: false
        });
      });
  },

  onLoadRepoBuildById(repoBuildId) {
    RepoBuildApi.fetchRepoBuildById(repoBuildId)
      .then((resp) => {
        this.trigger({
          repoBuild: resp,
          loading: false
        });
      });
  },

  onLoadModuleBuilds(params, cacheRepoBuildId = false) {
    this.params = params;

    if (this.isRequestingModuleBuilds) {
      return;
    }

    this.isRequestingModuleBuilds = true;

    const {branchId, buildNumber} = params;
    const fetchModuleBuildsPromise = cacheRepoBuildId && this._isRepoBuildPreviouslyFetched() ?
      RepoBuildApi.fetchModuleBuildsById(branchId, this.repoBuild.id, buildNumber) :
      RepoBuildApi.fetchModuleBuilds(params);

    fetchModuleBuildsPromise
      .then((resp) => {
        this.moduleBuilds = resp;
        this.isRequestingModuleBuilds = false;

        this.trigger({
          moduleBuilds: this.moduleBuilds,
          loadingModuleBuilds: false
        });
      }, (error) => {
        this.isRequestingModuleBuilds = false;
        this.trigger({
          error,
          loadingModuleBuilds: false
        });
      });
  },

  onLoadModuleBuildsById(branchId, repoBuildId, buildNumber) {
    RepoBuildApi.fetchModuleBuildsById(branchId, repoBuildId, buildNumber)
      .then((resp) => {
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

  onCancelBuild(repoBuildId) {
    RepoBuildApi.cancelBuild(repoBuildId);
  },

  _poll(cacheRepoBuildId = false) {
    // only cache the repoBuildId after the initial fetch each
    // time we start polling
    this.onLoadModuleBuilds(this.params, cacheRepoBuildId);
    this.onLoadRepoBuild(this.params, cacheRepoBuildId);

    if (this.shouldPoll) {
      setTimeout(() => this._poll(true), window.config.activeBuildModuleRefresh);
    }
  },

  /**
   * Helper function to avoid making extra api requests to
   * redetermine the repoBuildId for the current buildNumber
   */
  _isRepoBuildPreviouslyFetched() {
    const {branchId, buildNumber} = this.params;

    return this.repoBuild.branchId === parseInt(branchId, 10) &&
      this.repoBuild.buildNumber === parseInt(buildNumber, 10);
  }

});

export default RepoBuildStore;
