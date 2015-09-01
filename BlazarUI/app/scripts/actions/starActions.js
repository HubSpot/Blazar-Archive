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

StarActions.toggleStar = function(state, repo, branch) {
  StarProvider.starChange(state, repo, branch);
  StarActions.loadStarsSuccess(StarProvider.getStars());
};


export default StarActions;
