import Reflux from 'reflux';
import StarProvider from '../utils/starProvider';

const StarActions = Reflux.createActions([
  'loadStars',
  'loadStarsSuccess',
  'loadStarsError'
]);

StarActions.loadStars.preEmit = function() {
  StarActions.loadStarsSuccess(StarProvider.getStars());
};

StarActions.toggleStar = function(isStarred, starInfo) {
  const stars = StarProvider.starChange(isStarred, starInfo);
  StarActions.loadStarsSuccess(stars);
};


export default StarActions;
