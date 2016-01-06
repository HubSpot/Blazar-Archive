/*global config*/
import Reflux from 'reflux';

const BuildsActions = Reflux.createActions([
  'loadBuilds',
  'setFilterType',
  'stopPollingBuilds'
]);

export default BuildsActions;
