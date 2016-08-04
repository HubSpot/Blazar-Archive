import store from 'store';

const StarProvider = {

  haveSynced: false,
  stars: [],

  checkStorage() {
    if (!this.haveSynced) {
      this.getStars();
      this.haveSynced = true;
    }
  },

  toggleStar(repoId, cb) {
    const repoId = parseInt(repoId);
    this.checkStorage();
    const index = this.stars.indexOf(repoId);

    if (index !== -1) {
      this.stars.splice(index, 1);
    }
    else {
      this.stars.push(parseInt(repoId));
    }

    cb(this.stars);
    store.set('starredRepos', this.stars);
  },


  getStars() {
    if (this.haveSynced) {
      return this.stars;
    }

    this.haveSynced = true;
    return this.stars = store.get('starredRepos') || [];
  }

};

export default StarProvider;
