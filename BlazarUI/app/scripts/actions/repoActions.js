import Reflux from 'reflux';
import $ from 'jquery';
import Builds from '../collections/Builds';
import ActionSettings from './utils/ActionSettings';

let repoActionSettings = new ActionSettings;

let BranchActions = Reflux.createActions([
  'loadBranches',
  'loadBranchesSuccess',
  'loadBranchesError',
  'updatePollingStatus'
]);

BranchActions.loadBranches.preEmit = function(data) {
  startPolling(data);
};

BranchActions.updatePollingStatus = function (status) {
  repoActionSettings.setPolling(status);
};

function startPolling(data){

  (function doPoll(){
    let builds = new Builds();
    let promise = builds.fetch();

    promise.done( () => {
      let branches = builds.getBranchesByRepo(data);
      BranchActions.loadBranchesSuccess(branches);
    });

    promise.error( (err) => {
      console.warn('Error connecting to the API. Check that you are connected to the VPN');
      BranchActions.loadBranchesError('an error occured');
    })

    promise.always( () => {
      if (repoActionSettings.polling) {
        setTimeout(doPoll, config.buildsRefresh);
      }
    });

  })();

}

export default BranchActions;
