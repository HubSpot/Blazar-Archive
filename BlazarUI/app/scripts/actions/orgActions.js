/*global config*/
import Reflux from 'reflux';
import ActionSettings from './utils/ActionSettings';
import Builds from '../collections/Builds';
import BuildsStore from '../stores/buildsStore';

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

function startPolling(params) {
  (function doPoll() {
    const builds = new Builds();
    builds.set(BuildsStore.getBuilds());

    const repos = builds.getReposByOrg(params);
    OrgActions.loadReposSuccess(repos);

    if (orgActionSettings.polling) {
      setTimeout(doPoll, config.buildsRefresh);
    }
  })();
}


export default OrgActions;
