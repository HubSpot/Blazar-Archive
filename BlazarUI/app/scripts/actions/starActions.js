import Reflux from 'reflux';
import StarProvider from '../utils/starProvider';

const StarActions = Reflux.createActions([
  'loadStars',
  'loadStarsSuccess',
  'loadStarsError',
  'setSource'
]);

StarActions.loadStars.preEmit = function(source) {
  StarActions.setSource(source)
  StarActions.loadStarsSuccess(StarProvider.getStars());
};

StarActions.toggleStar = function(isStarred, starInfo) {
  const stars = StarProvider.starChange(isStarred, starInfo);
  StarActions.loadStarsSuccess(stars);
};


export default StarActions;
