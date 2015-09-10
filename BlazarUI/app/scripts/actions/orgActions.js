/*global config*/
import Reflux from 'reflux';
import ActionSettings from './utils/ActionSettings';
import Builds from '../collections/Builds';

const orgActionSettings = new ActionSettings;

const OrgActions = Reflux.createActions([
  'loadRepos',
  'loadReposSuccess',
  'loadReposError',
  'updatePollingStatus'
]);

OrgActions.loadRepos.preEmit = function(data) {
  startPolling(data);
};

OrgActions.updatePollingStatus = function(status) {
  orgActionSettings.setPolling(status);
};


function startPolling(data) {

  (function doPoll() {
    const builds = new Builds(data);
    const promise = builds.fetch();

    promise.done( () => {
      const repos = builds.getReposByOrg(data);
      OrgActions.loadReposSuccess(repos);
    });

    promise.error( () => {
      OrgActions.loadReposError('an error occured');
    });

    promise.always( () => {
      if (orgActionSettings.polling) {
        setTimeout(doPoll, config.buildsRefresh);
      }
    });

  })();

}


export default OrgActions;
