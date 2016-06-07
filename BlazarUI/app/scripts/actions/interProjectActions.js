import Reflux from 'reflux';

const InterProjectActions = Reflux.createActions([
  'triggerInterProjectBuild',
  'getInterProjectBuild',
  'getInterProjectBuildMappings',
  'getInterProjectBuildMappingsByRepoBuildId',
  'getUpAndDownstreamModules'
]);

export default InterProjectActions;
