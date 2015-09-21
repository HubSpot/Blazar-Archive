import Reflux from 'reflux';
import ActionSettings from './utils/ActionSettings';

const orgActionSettings = new ActionSettings;

const OrgActions = Reflux.createActions([
  'updatePollingStatus',
  'getRepos',
  'setParams'
]);

OrgActions.loadRepos = function(params) {
  OrgActions.setParams(params);
};

OrgActions.updatePollingStatus = function(status) {
  orgActionSettings.setPolling(status);
};

export default OrgActions;
