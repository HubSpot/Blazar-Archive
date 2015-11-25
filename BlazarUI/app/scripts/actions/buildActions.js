/*global config*/
import Reflux from 'reflux';

const BuildActions = Reflux.createActions([
  'loadBuild',
  'resetBuild',
  'triggerBuild',
  'cancelBuild',
  'fetchNext',
  'fetchPrevious',
  'navigationChange',
  'setLogPollingState'
]);

export default BuildActions;
