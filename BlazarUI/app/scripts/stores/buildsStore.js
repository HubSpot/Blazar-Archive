import Reflux from 'reflux';
import BuildsActions from '../actions/buildsActions';

function updateBuilds(latest, builds) {
  for (let i = 0, len = builds.length; i < len; i++) {
    for (let g = 0, len = latest.length; g < len; g++) {
      if (latest[g].module.id === builds[i].module.id) {
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
