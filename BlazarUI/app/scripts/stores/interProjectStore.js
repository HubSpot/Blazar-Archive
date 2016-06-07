/*global config*/
import Reflux from 'reflux';
import InterProjectActions from '../actions/interProjectActions';
import InterProjectApi from '../data/InterProjectApi';

import BranchActions from '../actions/branchActions';

const InterProjectStore = Reflux.createStore({

  listenables: InterProjectActions,

  init() {
    this.interProjectBuild = {};
    this.interProjectBuildMappings = [];
    this.interProjectBuildMappingByRepoBuildId = [];
    this.upAndDownstreamModules = {};
  },

  onTriggerInterProjectBuild(params, state) {
    const {selectedModules, buildDownstreamModules, resetCache} = state;
    InterProjectApi.triggerInterProjectBuild(selectedModules, resetCache, (error, resp) => {
      if (error) {
        this.error = error;
        return this.triggerErrorUpdate();
      }

      BranchActions.loadBranchBuildHistory(params);
    });
  },

  onGetInterProjectBuild(interProjectBuildId) {
    InterProjectApi.getInterProjectBuild(interProjectBuildId, (resp) => {
      this.interProjectBuild = resp;

      this.trigger({
        interProjectBuild: this.interProjectBuild
      });
    });
  },

  onGetInterProjectBuildMappingsByRepoBuildId(repoBuildId) {
    InterProjectApi.getInterProjectBuildMappingsByRepoBuildId(repoBuildId, (resp) => {
      this.interProjectBuildMappingByRepoBuildId = resp;

      this.trigger({
        interProjectBuildMappingByRepoBuildId: this.interProjectBuildMappingByRepoBuildId
      });
    });
  },

  onGetInterProjectBuildMappings(interProjectBuildId) {
    InterProjectApi.getInterProjectBuildMappings(interProjectBuildId, (resp) => {
      this.interProjectBuildMappings = resp;

      this.trigger({
        interProjectBuildMappings: this.interProjectBuildMappings
      });
    });
  },

  onGetUpAndDownstreamModules(repoBuildId) {
    InterProjectApi.getUpAndDownstreamModules(repoBuildId, (resp) => {
      this.upAndDownstreamModules = resp;

      this.trigger({
        upAndDownstreamModules: this.upAndDownstreamModules
      });
    });
  }
});

export default InterProjectStore;
