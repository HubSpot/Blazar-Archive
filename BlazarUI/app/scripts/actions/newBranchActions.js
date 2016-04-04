/*global config*/
import Reflux from 'reflux';

const NewBranchActions = Reflux.createActions([
  'loadBranchBuildHistory',
  'loadBranchInfo',
  'loadBranchModules',
  'loadBranchMalformedFiles',
  'startPolling',
  'stopPolling'
]);

export default NewBranchActions;
