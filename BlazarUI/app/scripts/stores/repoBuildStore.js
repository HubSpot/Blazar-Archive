//
// Holds individual modules builds triggered by a Repo Build
//
import Reflux from 'reflux';
import RepoBuildActions from '../actions/repoBuildActions';
import RepoBuildApi from '../data/RepoBuildApi';

const RepoBuildStore = Reflux.createStore({

  listenables: RepoBuildActions,

  getModuleBuilds() {
    return this.builds;
  },
  
  onStopPolling() {
    if (this.api) {
      this.api.stopPolling();  
    }
  },
  
  onCancelBuild() {
    this.api.cancelBuild();
  },

  onLoadMalformedFiles() {
    this.api.getMalformedFiles((resp) => {
      this.triggerMalformedFileUpdate(resp);
    });
  },

  onLoadModuleBuilds(params) {
    this.api = new RepoBuildApi(params);
    
    this.api.startPolling((err, resp) => {
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
      this.triggerUpdate();
    });    
  },
  
  triggerUpdate() {
    this.trigger({
      moduleBuilds: this.builds.moduleBuilds,
      currentRepoBuild: this.builds.currentRepoBuild,
      branchId: this.builds.branchId,
      loadingModuleBuilds: false
    });
  },
  
  triggerMalformedFileUpdate(resp) {
    this.trigger({
      malformedFiles: resp,
      loadingMalformedFiles: false
    });
  }

});

export default RepoBuildStore;
