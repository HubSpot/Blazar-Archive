import Reflux from 'reflux';
import StarProvider from '../services/starProvider';

const StarActions = Reflux.createActions([
  'loadStars',
  'loadStarsSuccess',
  'loadStarsError',
  'setSource'
]);

StarActions.loadStars.preEmit = (source = '') => {
  StarActions.setSource(source);
  StarActions.loadStarsSuccess(StarProvider.getStars());
};

StarActions.toggleStar = (repoId) => {
  StarProvider.toggleStar(repoId, (stars) => {
    StarActions.loadStarsSuccess(stars);
  });
};

export default StarActions;
