import Reflux from 'reflux';
import $ from 'jQuery';

import Build from '../models/Build';
import Log from '../models/Log';

let BuildActions = Reflux.createActions([
  'loadBuild',
  'loadBuildSuccess',
  'loadBuildError',
  'reloadBuild'
]);

BuildActions.loadBuild.preEmit = function(data) {
  fetchBuild(data);
};

BuildActions.reloadBuild = function (data) {
  fetchBuild(data)
};


function fetchBuild(data) {

  let build = new Build(data);
  let buildPromise = build.fetch();

  buildPromise.done( () => {

    if (build.data.build.state === 'IN_PROGRESS') {
      BuildActions.loadBuildSuccess({
        build: build.data
      });
      return;
    }

    let log = new Log(build.data.build.log);
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

}

export default BuildActions;
