import Reflux from 'reflux';

const InterProjectActions = Reflux.createActions([
  'triggerInterProjectBuild',
  'getUpAndDownstreamModules'
]);

export default InterProjectActions;
