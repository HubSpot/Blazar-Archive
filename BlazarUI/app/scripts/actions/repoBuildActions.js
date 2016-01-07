/*global config*/
import Reflux from 'reflux';

const RepoBuildActions = Reflux.createActions([
  'loadModuleBuilds',
  'stopPolling'
]);

export default RepoBuildActions;
