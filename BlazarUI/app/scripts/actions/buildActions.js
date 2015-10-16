import Reflux from 'reflux';

import Build from '../models/Build';
import Log from '../models/Log';
import BuildTrigger from '../models/BuildTrigger';
import BranchDefinition from '../models/BranchDefinition';
import BranchModules from '../collections/BranchModules';
import {find, has} from 'underscore';
import BuildStates from '../constants/BuildStates';
import BuildHistoryActions from '../actions/buildHistoryActions';

// builds we are polling
let builds = {};
// requested build params
let requestedBuild = {};

const BuildActions = Reflux.createActions([
  'loadBuild',
  'loadBuildSuccess',
  'loadBuildError',
  'reloadBuild',
  'triggerBuildSuccess',
  'triggerBuildError',
  'triggerBuildStart',
  'loadBuildCancelError',
  'loadBuildCancelled'
]);

BuildActions.loadBuild.preEmit = function(data) {
  BuildActions.setupBuildRequest(data);
};

BuildActions.reloadBuild = function(data) {
  BuildActions.setupBuildRequest(data);
};

BuildActions.setupBuildRequest = function(data) {
  requestedBuild.gitInfo = data;

  getBranchId()
    .then(getModule)
    .then(getBuild)
    .then(processBuild.bind(this));
};

BuildActions.stopWatchingBuild = function(buildId, moduleId) {
  if (builds[moduleId]) {
    builds[moduleId].isActive = false;
  }
  else {
    // Hack - if user tries to navigate too quickly from one
    // build to the next, we dont have time to load the moduleId
    // and check if we need to clear polling
    // To do: find a better approach
    window.location.reload();
  }
};

BuildActions.cancelBuild = function(buildId, moduleId) {
  builds[moduleId].isActive = false;

  const build = new Build;
  const cancel = build.cancel(buildId);
  
  cancel.done((d, t, j) => {
    if (j.status === 204) {
      BuildActions.loadBuildCancelled()
    }
  })
  
  cancel.error((err) => {
    BuildActions.loadBuildCancelError({
      status: err.status,
      responseText: JSON.parse(err.responseText).message
    });
  });
};

// To do: move into build model
BuildActions.triggerBuild = function(moduleId) {
  BuildActions.triggerBuildStart();
  const trigger = new BuildTrigger(moduleId);
  const promise = trigger.fetch();
  promise.then(() => {
    BuildActions.triggerBuildSuccess();
    BuildHistoryActions.fetchLatestHistory();
  },
  (data, textStatus, jqXHR) => {
    BuildActions.triggerBuildError(jqXHR);
  });
};

function getBranchId() {
  const branchDefinition = new BranchDefinition(requestedBuild.gitInfo);
  const branchPromise =  branchDefinition.fetch();

  branchPromise.done(() => {
    requestedBuild.gitInfo.branchId = branchDefinition.data.id;
  });
  branchPromise.error(() => {
    BuildHistoryActions.loadBuildHistoryError('an error occured');
  });

  return branchPromise;
}

function getModule() {
  const branchModules = new BranchModules(requestedBuild.gitInfo.branchId);
  const modulesPromise = branchModules.fetch();

  modulesPromise.done(() => {
    requestedBuild.gitInfo.moduleId = find(branchModules.data, (m) => {
      return m.name === requestedBuild.gitInfo.module;
    }).id;
  });
  modulesPromise.error(() => {
    BuildHistoryActions.loadBuildHistoryError('an error occured');
  });

  return modulesPromise;
}

function getBuild() {
  builds[requestedBuild.gitInfo.moduleId] = new Build(requestedBuild.gitInfo);
  builds[requestedBuild.gitInfo.moduleId].isActive = true;
  const buildPromise = builds[requestedBuild.gitInfo.moduleId].fetch();

  buildPromise.error(() => {
    BuildActions.loadBuildError("<p class='roomy-xy'>Error retrieving build log");
  });

  return buildPromise;
}

function processBuild() {
  const buildToProcess = builds[requestedBuild.gitInfo.moduleId];

  // to do: find out why we dont have state at this point
  if (!has(buildToProcess.data.build, 'state')) {
    return;
  }

  if (!buildToProcess.isActive) {
    return;
  }

  if (buildToProcess.data.build.state === BuildStates.LAUNCHING) {
    BuildActions.loadBuildSuccess({
      build: buildToProcess.data
    });
    return;
  }

  if (buildToProcess.data.build.state === BuildStates.IN_PROGRESS) {
    processInProgressBuild(buildToProcess);
  }

  else {
    processInactiveBuild(buildToProcess);
  }
}

function processInProgressBuild(build) {
  let inProgressBuild = {
    logLines: '',
    offset: 0
  };

  (function fetchLog() {
    if (!builds[build.data.module.id].isActive) {
      return;
    }

    const log = new Log({
      buildNumber: build.data.build.id,
      offset: inProgressBuild.offset
    });

    const logPromise = log.fetch();
    logPromise.always( (data, textStatus, jqxhr) => {
      inProgressBuild.logLines += log.formatLog(jqxhr);
      inProgressBuild.offset = data.nextOffset;

      if (data.nextOffset === -1) {
        // get latest build detail when log fetching is complete
        // so we can update status section at top of build page
        const lastBuild = new Build(requestedBuild.gitInfo);
        const buildPromise = lastBuild.fetch();

        buildPromise.done(() => {
          BuildActions.loadBuildSuccess({
            build: lastBuild.data,
            log: inProgressBuild.logLines,
            fetchingLog: false
          });
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
