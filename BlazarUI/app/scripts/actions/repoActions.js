/*global config*/
import Reflux from 'reflux';
import Builds from '../collections/Builds';
import ActionSettings from './utils/ActionSettings';

import BuildsStore from '../stores/buildsStore';

const repoActionSettings = new ActionSettings;

const RepoActions = Reflux.createActions([
  'loadBranches',
  'loadBranchesSuccess',
  'loadBranchesError',
  'updatePollingStatus'
]);


RepoActions.loadBranches.preEmit = function(params) {
  startPolling(params);
};

RepoActions.updatePollingStatus = function(status) {
  repoActionSettings.setPolling(status);
};

function startPolling(params) {

  (function doPoll() {
    const builds = new Builds();
    builds.set(BuildsStore.getBuilds());

    const branches = builds.getBranchesByRepo(params);
    RepoActions.loadBranchesSuccess(branches);

    if (repoActionSettings.polling) {
      setTimeout(doPoll, config.buildsRefresh);
    }


  })();

}

export default RepoActions;
