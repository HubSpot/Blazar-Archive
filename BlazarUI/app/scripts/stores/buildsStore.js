//
// Used to build sidebar
//
import Reflux from 'reflux';
import BuildsActions from '../actions/buildsActions';
import {sortBuilds} from '../utils/buildsHelpers';
import BuildsApi from '../data/BuildsApi';
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
    
    const ids = [22614011, 39576074];
    
    if (filter === 'starred') {
      BuildsApi.fetchStarredBuilds(ids, (err, resp) => {
        if (err) {
          console.warn(err);
        }
        this.trigger({
          builds: resp,
          loading: false,
          changingBuildsType: false
        });
      });
    }
    
    else {
      BuildsApi.fetchBuilds({filter: filter}, (err, resp) => {
        if (err) {
          // to do
        }
        this.builds[filter] = resp;

        this.trigger({
          builds: resp,
          loading: false,
          changingBuildsType: false
        });
      });  
    }
  
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
