/*global config*/
import Reflux from 'reflux';
import ActionSettings from './utils/ActionSettings';
import Builds from '../collections/Builds';
import BuildsStore from '../stores/buildsStore';

const BranchActionSettings = new ActionSettings;

const BranchActions = Reflux.createActions([
  'loadModules',
  'loadModulesSuccess',
  'loadModulesError',
  'updatePollingStatus'
]);

function startPolling(params) {

  (function doPoll() {
    const builds = new Builds();
    builds.set(BuildsStore.getBuilds());

    const branch = builds.getBranchModules(params);
    BranchActions.loadModulesSuccess(branch.modules || []);

    if (BranchActionSettings.polling) {
      setTimeout(doPoll, config.buildsRefresh);
    }
  })();

}

BranchActions.loadModules.preEmit = function(data) {
  startPolling(data);
};

BranchActions.updatePollingStatus = function(status) {
  BranchActionSettings.setPolling(status);
};

export default BranchActions;
