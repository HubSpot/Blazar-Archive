/*global config*/
import Reflux from 'reflux';
import ActionSettings from './utils/ActionSettings';
import Builds from '../collections/Builds';

const BranchActionSettings = new ActionSettings;

let BranchActions = Reflux.createActions([
  'loadModules',
  'loadModulesSuccess',
  'loadModulesError',
  'updatePollingStatus'
]);

function startPolling(data) {


  // TO DO:
  // DONT FETCH BUILDS HERE, USE BUILD STORE'S CACHE
  (function doPoll() {
    let builds = new Builds();
    let promise = builds.fetch();

    promise.done( () => {
      let branch = builds.getBranchModules(data);
      BranchActions.loadModulesSuccess(branch.modules || []);
    });

    promise.error( (err) => {
      console.warn('Error connecting to the API. Check that you are connected to the VPN.  ', err);
      BranchActions.loadModulesError('an error occured');
    });

    promise.always( () => {
      if (BranchActionSettings.polling) {
        setTimeout(doPoll, config.buildsRefresh);
      }
    });

  })();

}


BranchActions.loadModules.preEmit = function(data) {
  startPolling(data);
};

BranchActions.updatePollingStatus = function(status) {
  BranchActionSettings.setPolling(status);
};

export default BranchActions;
