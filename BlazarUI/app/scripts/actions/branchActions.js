import Reflux from 'reflux';

const BranchActions = Reflux.createActions([
  'loadBranchBuildHistory',
  'loadBranchInfo',
  'loadBranchModules',
  'loadMalformedFiles',
  'startPolling',
  'stopPolling'
]);

export default BranchActions;
