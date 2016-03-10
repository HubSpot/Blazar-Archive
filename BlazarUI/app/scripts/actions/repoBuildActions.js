/*global config*/
import Reflux from 'reflux';

const RepoBuildActions = Reflux.createActions([
  'loadModuleBuilds',
  'stopPolling',
  'cancelBuild',
  'loadMalformedFiles'
]);

export default RepoBuildActions;
