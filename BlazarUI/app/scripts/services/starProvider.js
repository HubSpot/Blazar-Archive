import store from 'store';

const StarProvider = {

  haveSynced: false,
  starStore: [],

  checkStorage: function() {
    if (!this.haveSynced) {
      this.getStars();
      this.haveSynced = true;
    }
  },

  toggleStar: function(repoId, cb) {
    const repoId = parseInt(repoId);
    this.checkStorage();
    const index = this.starStore.indexOf(repoId);
    
    if (index !== -1) {
      this.starStore.splice(index, 1);
    }
    else {
      this.starStore.push(parseInt(repoId));
    }

    cb(this.starStore);
    store.set('starredRepos', this.starStore);    
  },


  getStars: function() {
    this.haveSynced = true;
    return this.starStore = store.get('starredRepos') || [];
  }

};

export default StarProvider;
