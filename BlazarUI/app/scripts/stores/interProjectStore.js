import Reflux from 'reflux';
import InterProjectActions from '../actions/interProjectActions';
import InterProjectApi from '../data/InterProjectApi';

const InterProjectStore = Reflux.createStore({

  listenables: InterProjectActions,

  init() {
    this.upAndDownstreamModules = {};
    this.loadingUpAndDownstreamModules = true;
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
