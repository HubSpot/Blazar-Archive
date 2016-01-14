import Reflux from 'reflux';

const BranchActions = Reflux.createActions([
  'loadBranchBuilds',
  'stopPolling'
]);

export default BranchActions;
