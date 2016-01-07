//
// Holds individual modules builds triggered by a Repo Build
//
import Reflux from 'reflux';
import RepoBuildActions from '../actions/repoBuildActions';
import RepoBuildApi from '../data/RepoBuildApi';

const RepoBuildStore = Reflux.createStore({

  listenables: RepoBuildActions,

  init() {  
    this.moduleBuilds = null;
  },

  getModuleBuilds() {
    return this.builds;
  },
  
  onStopPolling() {
    RepoBuildApi.stopPolling();
  },

  onLoadModuleBuilds(repoBuildId) {
    RepoBuildApi.fetchModuleBuilds({repoBuildId: repoBuildId}, (err, resp) => {
      if (err) {
        this.trigger({
          loadingModuleBuilds: false,
          error: {
            status: err.status,
            statusText: err.statusText
          }
        });
        return;
      }

      this.builds = resp;

      this.trigger({
        moduleBuilds: resp,
        loadingModuleBuilds: false
      });
    });  
  
  }

});

export default RepoBuildStore;
