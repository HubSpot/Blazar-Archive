/*global BuildHistoryActions*/
import Reflux from 'reflux';
import _ from 'underscore';

import Build from '../models/Build';
import Log from '../models/Log';
import BranchDefinition from '../models/BranchDefinition';
import BranchModules from '../collections/BranchModules';

let gitInfo;

function fetchBuild(data) {
  gitInfo = data;
  getBranchId();
}

let BuildActions = Reflux.createActions([
  'loadBuild',
  'loadBuildSuccess',
  'loadBuildError',
  'reloadBuild'
]);

BuildActions.loadBuild.preEmit = function(data) {
  fetchBuild(data);
};

BuildActions.reloadBuild = function(data) {
  fetchBuild(data);
};

function getModule() {
  let branchModules = new BranchModules(gitInfo.branchId);
  let modulesPromise = branchModules.fetch();

  modulesPromise.done( () => {
    gitInfo.moduleId = _.find(branchModules.data, (m) => {
      return m.name === gitInfo.module;
    }).id;
    getBuild();
  });
  modulesPromise.error( () => {
    BuildHistoryActions.loadBuildHistoryError('an error occured');
  });
}

function getBranchId() {
    let branchDefinition = new BranchDefinition(gitInfo);
    let branchPromise =  branchDefinition.fetch();

    branchPromise.done( () => {
      gitInfo.branchId = branchDefinition.data.id;
      getModule();
    });
    branchPromise.error( () => {
      BuildHistoryActions.loadBuildHistoryError('an error occured');
    });
}


function getBuild() {
  let build = new Build(gitInfo);
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
      if (!logData.status || logData.status === 200) {
        BuildActions.loadBuildSuccess({
          build: build.data,
          log: logData.data
        });
      }
    });

    logPromise.error( () => {
      BuildActions.loadBuildError('Error retrieving build log');
    });

  });

  buildPromise.error( () => {
    BuildActions.loadBuildError('Error retrieving build');
  });

}

export default BuildActions;
