import Reflux from 'reflux';
import StarredBuildsActions from '../actions/starredBuildsActions';
// import {updateBuilds, sortBuilds} from '../utils/buildsHelpers';
import {sortBuilds} from '../utils/buildsHelpers';

const StarredBuildsStore = Reflux.createStore({

  listenables: StarredBuildsActions,

  init() {
    this.builds = {
      starred: { builds: [] },
      all: { builds: [] },
      building: { builds: [] }
    }
  },

  getBuilds() {
    return this.builds;
  },

  loadBuildsSuccess(incomingBuilds, filterType, filterHasChanged) {    
    this.builds[filterType].builds = incomingBuilds;

    let sortedBuilds;
    if (filterType === 'building') {
      sortedBuilds = sortBuilds(this.builds[filterType].builds, 'building');
    }
    else {
      sortedBuilds = sortBuilds(this.builds[filterType].builds, 'abc');
    }

    const triggerPayload = {
      builds: sortedBuilds,
      loadingBuilds: false
    }
    
    if (filterHasChanged) {
      triggerPayload.filterHasChanged = true;
    }

    this.trigger(triggerPayload);

  },

  loadBuildsError(error) {
    this.trigger({
      error: error,
      loadingBuilds: false
    });
  }

});

export default StarredBuildsStore;
