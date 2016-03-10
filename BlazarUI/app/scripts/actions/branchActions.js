import Reflux from 'reflux';

const BranchActions = Reflux.createActions([
  'loadBranchBuilds',
  'stopPolling',
  'triggerBuild',
  'loadModules',
  'loadMalformedFiles'
]);

export default BranchActions;
