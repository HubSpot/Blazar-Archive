/*global BuildHistoryActions*/
import Reflux from 'reflux';

import Build from '../models/Build';
import Log from '../models/Log';
import BuildTrigger from '../models/BuildTrigger';
import BranchDefinition from '../models/BranchDefinition';
import BranchModules from '../collections/BranchModules';
import {find} from 'underscore';
import BuildStates from '../constants/BuildStates';


const BuildActions = Reflux.createActions([
  'loadBuild',
  'loadBuildSuccess',
  'loadBuildError',
  'reloadBuild',
  'triggerBuildSuccess',
  'triggerBuildError',
  'triggerBuildStart'
]);

BuildActions.prepareBuildInfo = function(data) {
  BuildActions.data = {
    gitInfo: data
  };
  getBranchId();
};

BuildActions.loadBuild.preEmit = function(data) {
  BuildActions.prepareBuildInfo(data);
};

BuildActions.reloadBuild = function(data) {
  BuildActions.prepareBuildInfo(data);
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
  const branchModules = new BranchModules(BuildActions.data.gitInfo.branchId);
  const modulesPromise = branchModules.fetch();

  modulesPromise.done(() => {
    BuildActions.data.gitInfo.moduleId = find(branchModules.data, (m) => {
      return m.name === BuildActions.data.gitInfo.module;
    }).id;
    getBuild();
  });
  modulesPromise.error(() => {
    BuildHistoryActions.loadBuildHistoryError('an error occured');
  });
}

function getBranchId() {
  const branchDefinition = new BranchDefinition(BuildActions.data.gitInfo);
  const branchPromise =  branchDefinition.fetch();

  branchPromise.done(() => {
    BuildActions.data.gitInfo.branchId = branchDefinition.data.id;
    getModule();
  });
  branchPromise.error(() => {
    BuildHistoryActions.loadBuildHistoryError('an error occured');
  });
}

function getBuild() {
  const build = new Build(BuildActions.data.gitInfo);
  const buildPromise = build.fetch();

  buildPromise.done(() => {
    processBuild(build);
  });

  buildPromise.error(() => {
    BuildActions.loadBuildError("<p class='roomy-xy'>Error retrieving build log");
  });
}

function processBuild(build) {
  if (build.data.build.state === BuildStates.LAUNCHING) {
    BuildActions.loadBuildSuccess({
      build: build.data
    });
    return;
  }

  if (build.data.build.state === BuildStates.IN_PROGRESS) {
    processInProgressBuild(build);
  }

  else {
    processInactiveBuild(build);
  }
}

function processInProgressBuild(build) {
  let inProgressBuild = {
    active: false,
    logLines: '',
    offset: 0
  };

  (function fetchLog() {
    const log = new Log({
      buildNumber: build.data.build.id,
      offset: inProgressBuild.offset
    });

    const logPromise = log.fetch();
    logPromise.always( (data, textStatus, jqxhr) => {

      inProgressBuild.logLines += log.formatLog(jqxhr);
      inProgressBuild.offset = data.nextOffset;

      // build complete
      if (data.nextOffset === -1) {
        BuildActions.loadBuildSuccess({
          build: build.data,
          log: inProgressBuild.logLines,
          fetchingLog: false
        });
      }

      // still building
      else {
        BuildActions.loadBuildSuccess({
          build: build.data,
          log: inProgressBuild.logLines,
          fetchingLog: true
        });

        setTimeout(() => {
          fetchLog();
        }, 5000);
      }

    });

  })();
}

function processInactiveBuild(build) {
  let logLines = '';
  let offset = 0;

  (function fetchLog() {
    const log = new Log({
      buildNumber: build.data.build.id,
      offset: offset
    });

    const logPromise = log.fetch();
    logPromise.always( (data, textStatus, jqxhr) => {

      logLines += log.formatLog(jqxhr);

      if (jqxhr.responseJSON === undefined || textStatus !== 'success') {
        BuildActions.loadBuildSuccess({
          build: build,
          log: `<p class='roomy-xy'>${data.responseText}</p>`,
          fetchingLog: false
        });
        return;
      }

      BuildActions.loadBuildSuccess({
        build: build.data,
        log: logLines,
        fetchingLog: true
      });

      // more log lines exist
      if (jqxhr.responseJSON.nextOffset !== -1) {
        offset = jqxhr.responseJSON.nextOffset;
        fetchLog();
      }

      // got all log lines
      else {
        BuildActions.loadBuildSuccess({
          build: build.data,
          log: logLines,
          fetchingLog: false
        });
      }

    });

  })();
}

export default BuildActions;
