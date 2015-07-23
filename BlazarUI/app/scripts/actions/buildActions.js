import Reflux from 'reflux';
import $ from 'jQuery';

import Build from '../models/Build';
import Log from '../models/Log';

let BuildActions = Reflux.createActions([
  'loadBuild',
  'loadBuildSuccess',
  'loadBuildError'
]);

BuildActions.loadBuild.preEmit = function(data) {

  let build = new Build(data);
  let buildPromise = build.fetch();

  buildPromise.done( () => {
    let log = new Log(build.data.buildState.buildLog);
    let logPromise = log.fetch();

    logPromise.always( (logData) => {
      BuildActions.loadBuildSuccess({
        build: build.data,
        log: logData
      });

    });

    logPromise.error( () => {
      BuildActions.loadBuildError('Error retrieving build log');
    })

  })

  buildPromise.error( () => {
    BuildActions.loadBuildError('Error retrieving build');
  })


};

export default BuildActions;
