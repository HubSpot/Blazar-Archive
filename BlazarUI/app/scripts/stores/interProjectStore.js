import Reflux from 'reflux';
import InterProjectActions from '../actions/interProjectActions';
import InterProjectApi from '../data/InterProjectApi';

import BranchActions from '../actions/branchActions';

const InterProjectStore = Reflux.createStore({

  listenables: InterProjectActions,

  init() {
    this.upAndDownstreamModules = {};
    this.loadingUpAndDownstreamModules = true;
  },

  onTriggerInterProjectBuild(params, state) {
    const {selectedModules, resetCache} = state;
    InterProjectApi.triggerInterProjectBuild(selectedModules, resetCache, (error) => {
      if (error) {
        this.error = error;
        this.triggerErrorUpdate();
        return;
      }

      BranchActions.loadBranchBuildHistory(params);
    });
  },

  triggerErrorUpdate() {
    this.trigger({
      error: this.error
    });

    this.error = undefined;
  },

  onGetUpAndDownstreamModules(repoBuildId) {
    InterProjectApi.getUpAndDownstreamModules(repoBuildId, (resp) => {
      this.upAndDownstreamModules = resp;

      this.trigger({
        upAndDownstreamModules: this.upAndDownstreamModules,
        loadingUpAndDownstreamModules: false
      });
    });
  }
});

export default InterProjectStore;
