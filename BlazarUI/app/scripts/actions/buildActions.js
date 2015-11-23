/*global config*/
import Reflux from 'reflux';

const BuildActions = Reflux.createActions([
  'triggerBuild', // to do
  'loadBuild',
  'resetBuild',
  'fetchNext',
  'fetchPrevious',
  'navigationChange'
]);

export default BuildActions;
