import {union} from 'underscore';
import store from 'store';

const StarProvider = {

  haveSynced: false,
  starCache: [],

  checkStorage: function() {
    if (!this.haveSynced) {
      this.getStars();
      this.haveSynced = true;
    }
  },

  starChange: function(isStarred, moduleId) {
    this.checkStorage();

    if (isStarred) {
      this.removeStar(moduleId);
    } else {
      this.addStar(moduleId);
    }
    return this.starCache;
  },

  addStar: function(moduleId) {
    this.starCache = union(this.starCache, [moduleId]);
    this.updateStore();
  },

  removeStar: function(moduleId) {
    const i = this.starCache.indexOf(moduleId);
    if (i !== -1) {
      this.starCache.splice(i, 1);
    }
    this.updateStore();
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
