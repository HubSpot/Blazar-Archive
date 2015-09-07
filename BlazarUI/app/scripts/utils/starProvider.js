import {chain, union} from 'underscore';
import store from 'store';

const StarProvider = {

  haveSynced: false,
  starCache: [],
  starInfo: {},

  checkStorage: function() {
    if (!this.haveSynced) {
      this.getStars();
      this.haveSynced = true;
    }
  },

  starChange: function(isStarred, starInfo) {
    this.checkStorage();

    this.starInfo = starInfo;

    if (isStarred) {
      this.removeStar();
    } else {
      this.addStar();
    }
    return this.starCache;
  },

  addStar: function() {
    this.starCache = union(this.starCache, [this.starInfo]);
    this.updateStore();
  },

  removeStar: function() {
    const indx = chain(this.starCache)
                .pluck('moduleId')
                .indexOf(this.starInfo.moduleId)
                .value();

    if (indx !== -1) {
      this.starCache.splice(indx, 1);
      this.updateStore();
    }

  },

  updateStore: function() {
    store.set('starredModules', this.starCache);
  },

  getStars: function() {
    if (this.haveSynced) {
      return this.starCache;
    }

    this.starCache = store.get('starredModules') || [];
    this.haveSynced = true;

    return this.starCache;
  }

};

window.StarProvider = StarProvider;

export default StarProvider;
