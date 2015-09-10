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

const BuildActions = Reflux.createActions([
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
  const branchModules = new BranchModules(gitInfo.branchId);
  const modulesPromise = branchModules.fetch();

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
    const branchDefinition = new BranchDefinition(gitInfo);
    const branchPromise =  branchDefinition.fetch();

    branchPromise.done( () => {
      gitInfo.branchId = branchDefinition.data.id;
      getModule();
    });
    branchPromise.error( () => {
      BuildHistoryActions.loadBuildHistoryError('an error occured');
    });
}


function getBuild() {
  const build = new Build(gitInfo);
  const buildPromise = build.fetch();

  buildPromise.done( () => {

    if (build.data.build.state === 'LAUNCHING') {
      BuildActions.loadBuildSuccess({
        build: build.data
      });
      return;
    }

    const log = new Log(build.data.build.id);
    const logPromise = log.fetch();

    logPromise.always( (logData) => {
      if (!logData) {
        logData.data = '';
      }

      BuildActions.loadBuildSuccess({
        build: build.data,
        log: logData.data
      });

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
