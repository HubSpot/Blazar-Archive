import Reflux from 'reflux';
import $ from 'jquery';
import ActionSettings from './utils/ActionSettings';
import Builds from '../collections/Builds';

let orgActionSettings = new ActionSettings;

let OrgActions = Reflux.createActions([
  'loadRepos',
  'loadReposSuccess',
  'loadReposError',
  'updatePollingStatus'
]);

OrgActions.loadRepos.preEmit = function(data) {
  startPolling(data);
};

OrgActions.updatePollingStatus = function (status) {
  orgActionSettings.setPolling(status);
};


function startPolling(data){

  (function doPoll(){
    let builds = new Builds(data);
    let promise = builds.fetch();

    promise.done( () => {
      let repos = builds.getReposByOrg(data);
      OrgActions.loadReposSuccess(repos);
    });

    promise.error( () => {
      OrgActions.loadReposError('an error occured');
    })

    promise.always( () => {
      if (orgActionSettings.polling) {
        setTimeout(doPoll, config.buildsRefresh);
      }
    });

  })();

}


export default OrgActions;
