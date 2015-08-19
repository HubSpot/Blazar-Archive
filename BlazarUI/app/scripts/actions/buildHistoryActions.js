import Reflux from 'reflux';
import $ from 'jquery';
import ActionSettings from './utils/ActionSettings';
import BuildHistory from '../collections/BuildHistory';

let buildHistoryActionSettings = new ActionSettings;

let BuildHistoryActions = Reflux.createActions([
  'loadBuildHistory',
  'loadBuildHistorySuccess',
  'loadBuildHistoryError',
  'updatePollingStatus'
]);

BuildHistoryActions.loadBuildHistory.preEmit = function(data) {
  startPolling(data);
};

BuildHistoryActions.updatePollingStatus = function (status) {
  buildHistoryActionSettings.setPolling(status);
};


function startPolling(data){

  (function doPoll(){
    let buildHistory = new BuildHistory(data);
    let promise = buildHistory.fetch();

    promise.done( () => {
      BuildHistoryActions.loadBuildHistorySuccess(buildHistory.data);
    });

    promise.error( () => {
      BuildHistoryActions.loadBuildHistoryError('an error occured');
    })

    promise.always( () => {
      if (buildHistoryActionSettings.polling) {
        setTimeout(doPoll, config.buildsRefresh);
      }
    });

  })();

}


export default BuildHistoryActions;
