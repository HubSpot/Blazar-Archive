import Reflux from 'reflux';

const BranchActions = Reflux.createActions([
  'loadBranchBuilds',
  'stopPolling',
  'triggerBuild',
  'loadModules'
]);

export default BranchActions;
