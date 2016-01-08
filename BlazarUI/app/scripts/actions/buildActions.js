/*global config*/
import Reflux from 'reflux';

const BuildActions = Reflux.createActions([
  'loadBuild',
  'resetBuild',
  'fetchNext',
  'fetchPrevious',
  'navigationChange',
  'setLogPollingState'
]);

export default BuildActions;
