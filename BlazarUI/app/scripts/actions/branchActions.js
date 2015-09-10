/*global config*/
import Reflux from 'reflux';
import ActionSettings from './utils/ActionSettings';
import Builds from '../collections/Builds';

const BranchActionSettings = new ActionSettings;

const BranchActions = Reflux.createActions([
  'loadModules',
  'loadModulesSuccess',
  'loadModulesError',
  'updatePollingStatus'
]);

function startPolling(data) {


  // TO DO:
  // DONT FETCH BUILDS HERE, USE BUILD STORE'S CACHE
  (function doPoll() {
    const builds = new Builds();
    const promise = builds.fetch();

    promise.done( () => {
      const branch = builds.getBranchModules(data);
      BranchActions.loadModulesSuccess(branch.modules || []);
    });

    promise.error( () => {
      console.warn('Error connecting to the API. Check that you are connected to the VPN.');
      BranchActions.loadModulesError('error');
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
