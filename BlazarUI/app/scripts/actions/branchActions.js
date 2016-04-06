import Reflux from 'reflux';

const BranchActions = Reflux.createActions([
  'loadBranchBuildHistory',
  'loadBranchInfo',
  'loadBranchModules',
  'loadMalformedFiles',
  'startPolling',
  'stopPolling',
  'triggerBuild'
]);

export default BranchActions;
