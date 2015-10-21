import Reflux from 'reflux';
import GlobalBuildsActions from '../actions/globalBuildsActions';

const GlobalBuildsStore = Reflux.createStore({

  listenables: GlobalBuildsActions,

  init() {
    this.builds = [];
    this.hasLoaded = false;
  },
  
  loadBuildsOnceSuccess(incomingData) {
    this.builds = incomingData;
    this.hasLoaded = true;
  },
  
  triggerLoad() {
    this.triggerPayload();
  },

  loadBuildsSuccess(incomingData) {
    this.builds = incomingData;
    this.triggerPayload();
    this.hasLoaded = true;
  },
  
  triggerPayload() {
    this.trigger({
      builds: this.builds,
      loadingBuilds: false,
      changingBuildsType: false
    });
  }

});

export default GlobalBuildsStore;
