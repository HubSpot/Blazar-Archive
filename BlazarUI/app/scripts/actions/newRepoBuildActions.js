/*global config*/
import Reflux from 'reflux';

const NewRepoBuildActions = Reflux.createActions([
  'loadRepoBuild',
  'loadModuleBuilds',
  'startPolling',
  'stopPolling'
]);

export default NewRepoBuildActions;