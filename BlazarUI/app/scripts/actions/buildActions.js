import Reflux from 'reflux';
import $ from 'jQuery';

import Build from '../models/Build';

let BuildActions = Reflux.createActions([
  'loadBuild',
  'loadBuildSuccess',
  'loadBuildError'
]);

BuildActions.loadBuild.preEmit = function(data) {
  let build = new Build(data);
  let promise = build.fetch();

  promise.done( () => {
    BuildActions.loadBuildSuccess(build.data.data);
  });

  promise.error( () => {
    BuildActions.loadBuildError('an error occured');
  })

};

export default BuildActions;
