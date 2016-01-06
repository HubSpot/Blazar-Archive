//
// Used to build sidebar
//
import Reflux from 'reflux';
import BuildsActions from '../actions/buildsActions';
import {sortBuilds} from '../utils/buildsHelpers';
import BuildsApi from '../data/BuildsApi';
import Immutable from 'immutable';
// import StarStore from '../stores/starStore';

const BuildsStore = Reflux.createStore({

  listenables: BuildsActions,

  init() {  
    this.builds = {
      starred: {},
      all: { },
      building: {}
    };
  },

  getBuilds() {
    return this.builds;
  },
  
  onStopPollingBuilds() {
    BuildsApi.stopPolling;
  },

  onLoadBuilds(filter) {

    BuildsApi.fetchBuilds({filter: filter}, (err, resp) => {
      if (err) {
        this.trigger({
          loading: false,
          changingBuildsType: false,
          error: {
            status: err.status,
            statusText: err.statusText
          }
        });
        return;
      }
      this.builds[filter] = resp;

      this.trigger({
        builds: resp,
        loading: false,
        changingBuildsType: false
      });
    });  
  
  },
  
  onLoadBuildsCompleted: function() {
    console.log('complete!');
  },
  
  onLoadBuildsFailed: function() {
    console.log('failed :/');
  },
   
  onSetFilterType() {
    console.log('set filter!');
  }

});

export default BuildsStore;
