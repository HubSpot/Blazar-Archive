import Reflux from 'reflux';
import StarActions from '../actions/starActions';

const RepoStore = Reflux.createStore({

  listenables: StarActions,

  init() {
    this.stars = [];
    this.source = null;
  },
  
  setSource(source) {
    this.source = source;
  },

  loadStarsSuccess(stars) {
    this.stars = stars;

    this.trigger({
      source: this.source,
      stars: this.stars,
      loadingStars: false
    });
  },

  loadStarsError(error) {
    this.trigger({
      error: error,
      loadingStars: false
    });
  }

});

export default RepoStore;
