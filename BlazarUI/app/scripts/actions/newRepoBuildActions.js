/*global config*/
import Reflux from 'reflux';

const NewRepoBuildActions = Reflux.createActions([
  'loadRepoBuild',
  'loadModuleBuilds'
]);

export default NewRepoBuildActions;