import Reflux from 'reflux';
import $ from 'jQuery';

import BuildHistory from '../collections/BuildHistory';

let BuildHistoryActions = Reflux.createActions([
  'loadBuildHistory',
  'loadBuildHistorySuccess',
  'loadBuildHistoryError'
]);

BuildHistoryActions.loadBuildHistory.preEmit = function(data) {

  let buildHistory = new BuildHistory(data);
  let promise = buildHistory.fetch();

  promise.done( () => {
    let builds = {}
    BuildHistoryActions.loadBuildHistorySuccess(buildHistory.data);
  });

  promise.error( () => {
    BuildHistoryActions.loadBuildHistoryError('an error occured');
  })


};

export default BuildHistoryActions;
