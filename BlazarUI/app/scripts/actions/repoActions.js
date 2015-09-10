/*global config*/
import Reflux from 'reflux';
import Builds from '../collections/Builds';
import ActionSettings from './utils/ActionSettings';

const repoActionSettings = new ActionSettings;

const BranchActions = Reflux.createActions([
  'loadBranches',
  'loadBranchesSuccess',
  'loadBranchesError',
  'updatePollingStatus'
]);

BranchActions.loadBranches.preEmit = function(data) {
  startPolling(data);
};

BranchActions.updatePollingStatus = function(status) {
  repoActionSettings.setPolling(status);
};

function startPolling(data) {

  (function doPoll() {
    const builds = new Builds();
    const promise = builds.fetch();

    promise.done( () => {
      const branches = builds.getBranchesByRepo(data);
      BranchActions.loadBranchesSuccess(branches);
    });

    promise.error( () => {
      console.warn('Error connecting to the API. Check that you are connected to the VPN');
      BranchActions.loadBranchesError('an error occured');
    });

    promise.always( () => {
      if (repoActionSettings.polling) {
        setTimeout(doPoll, config.buildsRefresh);
      }
    });

  })();

}

export default BranchActions;
