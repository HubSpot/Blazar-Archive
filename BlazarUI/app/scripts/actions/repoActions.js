import Reflux from 'reflux';
import ActionSettings from './utils/ActionSettings';

const repoActionSettings = new ActionSettings;

const RepoActions = Reflux.createActions([
  'updatePollingStatus',
  'getBranches',
  'setParams'
]);

RepoActions.loadBranches = function(params) {
  RepoActions.setParams(params);
};

RepoActions.updatePollingStatus = function(status) {
  repoActionSettings.setPolling(status);
};

export default RepoActions;
