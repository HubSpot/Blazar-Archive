//
// Used for sidebar
//
import Reflux from 'reflux';
import BuildsActions from '../actions/buildsActions';
import BuildsApi from '../data/BuildsApi';
import {isEmpty} from 'underscore';

const BuildsStore = Reflux.createStore({

  listenables: BuildsActions,

  init() {
    this.builds = {
      all: [],
      building: [],
      starred: []
    };
  },

  getBuilds() {
    return this.builds;
  },

  onStopPollingBuilds() {
    BuildsApi.stopPolling();
  },

  onLoadBuilds(params) {
    const extraData = isEmpty(params);
    BuildsApi.fetchBuilds(extraData, (err, resp) => {
      if (err) {
        this.trigger({
          loading: false,
          dontDisplay: false,
          changingBuildsType: false,
          error: {
            status: err.status,
            statusText: err.statusText
          }
        });
        return;
      }
      this.builds = resp;

      this.trigger({
        builds: this.builds,
        loading: false,
        dontDisplay: false,
        changingBuildsType: false
      });
    });
  }

});

export default BuildsStore;
