import Reflux from 'reflux';
import StarActions from '../actions/starActions';

const RepoStore = Reflux.createStore({

  init() {
    this.stars = [];

    this.listenTo(StarActions.loadStars, this.loadStars);
    this.listenTo(StarActions.loadStarsSuccess, this.loadStarsSuccess);
    this.listenTo(StarActions.loadStarsError, this.loadStarsError);
  },

  loadStars() {
    this.trigger({
      loadingStars: true
    });
  },

  loadStarsSuccess(stars) {
    this.stars = stars;

    this.trigger({
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
