/*global config*/
import Reflux from 'reflux';

const BuildsActions = Reflux.createActions([
  'loadBuilds',
  'loadBuildsForDashboard',
  'stopPollingBuilds'
]);

export default BuildsActions;
