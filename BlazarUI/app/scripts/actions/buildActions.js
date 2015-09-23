/*global BuildHistoryActions*/
import Reflux from 'reflux';

import Build from '../models/Build';
import Log from '../models/Log';
import BuildTrigger from '../models/BuildTrigger';
import BranchDefinition from '../models/BranchDefinition';
import BranchModules from '../collections/BranchModules';
import {find} from 'underscore';

import BuildStates from '../constants/BuildStates';

let gitInfo;

function fetchBuild(data) {
  gitInfo = data;
  getBranchId();
}

const BuildActions = Reflux.createActions([
  'loadBuild',
  'loadBuildSuccess',
  'loadBuildError',
  'reloadBuild',
  'triggerBuildSuccess',
  'triggerBuildError',
  'triggerBuildStart'
]);

BuildActions.loadBuild.preEmit = function(data) {
  fetchBuild(data);
};

BuildActions.reloadBuild = function(data) {
  fetchBuild(data);
};

BuildActions.triggerBuild = function(moduleId) {
  BuildActions.triggerBuildStart();
  const trigger = new BuildTrigger(moduleId);
  const promise = trigger.fetch();
  promise.then(() => {
    BuildActions.triggerBuildSuccess();
  },
  (data, textStatus, jqXHR) => {
    BuildActions.triggerBuildError(jqXHR);
  });
};

function getModule() {
  const branchModules = new BranchModules(gitInfo.branchId);
  const modulesPromise = branchModules.fetch();

  modulesPromise.done(() => {
    gitInfo.moduleId = find(branchModules.data, (m) => {
      return m.name === gitInfo.module;
    }).id;
    getBuild();
  });
  modulesPromise.error(() => {
    BuildHistoryActions.loadBuildHistoryError('an error occured');
  });
}

function getBranchId() {
  const branchDefinition = new BranchDefinition(gitInfo);
  const branchPromise =  branchDefinition.fetch();

  branchPromise.done(() => {
    gitInfo.branchId = branchDefinition.data.id;
    getModule();
  });
  branchPromise.error(() => {
    BuildHistoryActions.loadBuildHistoryError('an error occured');
  });
}

function getBuild() {
  const build = new Build(gitInfo);
  const buildPromise = build.fetch();

  buildPromise.done( () => {
    if (build.data.build.state === BuildStates.LAUNCHING) {
      BuildActions.loadBuildSuccess({
        build: build.data
      });
      return;
    }

    const log = new Log(build.data.build.id);
    const logPromise = log.fetch();

    logPromise.always( (data, textStatus, jqxhr) => {

      BuildActions.loadBuildSuccess({
        build: build.data,
        log: log.formatLog(jqxhr)
      });

    });

    logPromise.error( () => {
      BuildActions.loadBuildError("<p class='roomy-xy'>No build log");
    });

  });

  buildPromise.error( () => {
    BuildActions.loadBuildError("<p class='roomy-xy'>Error retrieving build");
  });

}

export default BuildActions;
