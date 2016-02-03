import Reflux from 'reflux';

const BranchActions = Reflux.createActions([
  'loadBranchBuilds',
  'stopPolling',
  'triggerBuild'
]);

export default BranchActions;
