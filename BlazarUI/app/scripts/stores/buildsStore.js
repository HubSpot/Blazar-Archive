import Reflux from 'reflux';
import BuildsActions from '../actions/buildsActions';
import {pluck} from 'underscore';


function getBuildsIds(builds) {
  return pluck(pluck(builds, 'module'), 'id');
}

function updateBuilds(latest, builds) {

  const latestIds = getBuildsIds(latest);

  for (let i = 0, len = builds.length; i < len; i++) {
    for (let g = 0, len = latestIds.length; g < len; g++) {
      if (latestIds[g] === builds[i].module.id) {
        builds[i] = latest[g];
        break;
      }
    }
  }

  return builds;

}


const BuildsStore = Reflux.createStore({

  listenables: BuildsActions,

  init() {
    this.builds = [];
    this.buildsLoaded = false;
    this.buildsHaveLoaded = false;
  },

  getBuilds() {
    return this.builds;
  },

  loadBuilds() {
    this.trigger({
      loadingBuilds: true
    });
  },

  loadBuildsSuccess(incomingBuilds) {

    // first fetch
    if (!this.buildsHaveLoaded) {
      this.builds = incomingBuilds;
    }

    // subsequent fetches
    else {
      const updatedBuilds = updateBuilds(incomingBuilds, this.builds);
      this.builds = updatedBuilds;
    }

    this.trigger({
      builds: this.builds,
      loadingBuilds: false
    });

    this.buildsHaveLoaded = true;
  },

  loadBuildsError(error) {
    this.trigger({
      error: error,
      loadingBuilds: false
    });
  }

});

export default BuildsStore;
