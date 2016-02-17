import Reflux from 'reflux';

const BranchActions = Reflux.createActions([
  'loadBranchBuilds',
  'stopPolling',
  'triggerBuild',
  'triggerBuildModuleSpecific',
  'loadModules'
]);

export default BranchActions;
