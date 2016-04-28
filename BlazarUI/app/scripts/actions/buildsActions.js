/*global config*/
import Reflux from 'reflux';

const BuildsActions = Reflux.createActions([
  'loadBuilds',
  'stopPollingBuilds'
]);

export default BuildsActions;
