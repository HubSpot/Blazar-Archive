/* global config*/
import Reflux from 'reflux';

const RepoBuildActions = Reflux.createActions([
  'loadRepoBuild',
  'loadRepoBuildById',
  'loadModuleBuilds',
  'loadModuleBuildsById',
  'startPolling',
  'stopPolling',
  'cancelBuild'
]);

export default RepoBuildActions;
