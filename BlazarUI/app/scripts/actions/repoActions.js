/*global config*/
import Reflux from 'reflux';
import Builds from '../collections/Builds';
import ActionSettings from './utils/ActionSettings';

import BuildsStore from '../stores/buildsStore';

const repoActionSettings = new ActionSettings;

const BranchActions = Reflux.createActions([
  'loadBranches',
  'loadBranchesSuccess',
  'loadBranchesError',
  'updatePollingStatus'
]);


BranchActions.loadBranches.preEmit = function(params) {
  startPolling(params);
};

BranchActions.updatePollingStatus = function(status) {
  repoActionSettings.setPolling(status);
};

function startPolling(params) {

  (function doPoll() {
    const builds = new Builds();
    builds.set(BuildsStore.getBuilds().all);

    const branches = builds.getBranchesByRepo(params);
    BranchActions.loadBranchesSuccess(branches);

    if (repoActionSettings.polling) {
      setTimeout(doPoll, config.buildsRefresh);
    }


  })();

}

export default BranchActions;
