import Reflux from 'reflux';
import ActionSettings from './utils/ActionSettings';

const BranchActionSettings = new ActionSettings;

const BranchActions = Reflux.createActions([
  'updatePollingStatus',
  'getModules',
  'setParams'
]);

BranchActions.loadModules = function(params) {
  BranchActions.setParams(params);
};

BranchActions.updatePollingStatus = function(status) {
  BranchActionSettings.setPolling(status);
};

export default BranchActions;
