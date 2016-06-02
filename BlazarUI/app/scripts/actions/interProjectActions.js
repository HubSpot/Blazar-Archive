import Reflux from 'reflux';

const InterProjectActions = Reflux.createActions([
  'triggerInterProjectBuild',
  'getInterProjectBuild',
  'getInterProjectBuildMappings'
]);

export default InterProjectActions;
