/*global config*/
import Reflux from 'reflux';
import InterProjectActions from '../actions/interProjectActions';

const InterProjectStore = Reflux.createStore({

  listenables: InterProjectActions,

  init() {},

  onTriggerInterProjectBuild(params, state) {
    const {selectedModules, buildDownstreamModules, resetCache} = state;
    InterProjectActions.triggerInterProjectBuild(selectedModules, resetCache, (error, resp) => {
      if (error) {
        this.error = error;
        return this.triggerErrorUpdate();
      }
    });
  }
});

export default InterProjectStore;
