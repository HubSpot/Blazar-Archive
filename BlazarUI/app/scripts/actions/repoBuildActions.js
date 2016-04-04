/*global config*/
import Reflux from 'reflux';

const RepoBuildActions = Reflux.createActions([
  'loadRepoBuild',
  'loadModuleBuilds',
  'startPolling',
  'stopPolling',
  'cancelBuild'
]);

export default RepoBuildActions;