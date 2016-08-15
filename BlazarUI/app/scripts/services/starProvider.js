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
    const parsedRepoId = parseInt(repoId, 10);
    this.checkStorage();
    const index = this.stars.indexOf(parsedRepoId);

    if (index !== -1) {
      this.stars.splice(index, 1);
    } else {
      this.stars.push(parsedRepoId);
    }

    cb(this.stars);
    store.set('starredRepos', this.stars);
  },


  getStars() {
    if (this.haveSynced) {
      return this.stars;
    }

    this.haveSynced = true;
    this.stars = store.get('starredRepos') || [];
    return this.stars;
  }

};

export default StarProvider;
