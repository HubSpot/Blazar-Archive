/*global config*/
import Reflux from 'reflux';

import Build from '../models/Build';
import Log from '../models/Log';
import LogSize from '../models/LogSize';

import BuildTrigger from '../models/BuildTrigger';
import BranchDefinition from '../models/BranchDefinition';
import BranchModules from '../collections/BranchModules';
import {find, has} from 'underscore';
import BuildStates from '../constants/BuildStates';
import BuildHistoryActions from '../actions/buildHistoryActions';

// map to store builds and keep 
// track of if we are polling a build
let builds = {};
// temporary storage for a build we are 
// loading details for
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
  'loadBuildCancelled',
  'changeOffsetWithNavigation',
  'pageLog'
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
    .then(getLogSize)
    .then(processBuild.bind(this));
};

// Scrolling up or down log
BuildActions.pageLog = function(moduleId, direction) {
  loadScrollChangeOffset(builds[moduleId], direction);
};

// Using log navigation buttons
BuildActions.changeOffsetWithNavigation = function(moduleId, position) {
  loadNavigationChangeOffset({
    build: builds[moduleId],
    position: position
  });
}

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

  const build = new Build({
    buildId: buildId
  });

  const cancel = build.cancel();
  
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
  
  const trigger = new BuildTrigger({
    moduleId: moduleId
  });
  
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
  const branchModules = new BranchModules({
    branchId: requestedBuild.gitInfo.branchId
  });

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

function getLogSize() {  
  const build = builds[requestedBuild.gitInfo.moduleId];
  
  const logSize = new LogSize({
    buildNumber: build.data.build.id
  });

  const sizePromise = logSize.fetch();
  
  sizePromise.done((size) => {
    requestedBuild.logSize = size;
    build.log = createLogModel(build, requestedBuild.logSize);
  });

  return sizePromise;
}

function processBuild() {
  const buildToProcess = builds[requestedBuild.gitInfo.moduleId];

  // To do: find out why we dont have state at this point
  if (!has(buildToProcess.data.build, 'state')) {
    return;
  }

  if (!buildToProcess.isActive) {
    return;
  }

  // TO DO
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
      dataType: 'json',
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
  const log = build.log;
  const logPromise = log.fetch();
  logPromise.always((data, textStatus, jqxhr) => {
    build.log.nextOffset = data.nextOffset;
    build.log.currentPage = build.log.options.logPages;
    updateStore(build, log, data, textStatus, jqxhr)
  });  
}

//
// Shared Methods Across Build States
//
function createLogModel(build, size) {
  const maxOffset = Math.max(size - config.offsetLength, 0);

  return new Log({
    buildNumber: build.data.build.id,
    logSize: size,
    logPages: Math.ceil(maxOffset / 90000),
    lastOffset: maxOffset,
    offset: maxOffset
  });
}

// fetch previous/next offsets when scrolling
function loadScrollChangeOffset(build, direction) {

  build.hasNavigatedWithButtons = false;

  if (direction === 'down') {
    // if we are at the end of the log,
    // or started at the end and since scrolled up    
    if (build.log.endOfLogLoaded) {
      return;
    }

    build.log.incrementPage();
  }

  if (direction === 'up') {
    if (build.log.currentPage === 0) {
      return;
    }
    build.log.decrementPage();
  }

  build.log.direction = direction;
  build.log.positionChange = undefined;

  build.log.pageLog(direction).fetch().always((data, textStatus, jqxhr) => {
    updateStore(build, build.log, data, textStatus, jqxhr)
  });
}

// fetch specific offset when using Top/Bottom navigation buttons
function loadNavigationChangeOffset(options) {

  const { build, position } = options;
  // set the offset we will fetch
  // 0 for beginning, lastOffset for end of log
  const updatedOffset = position === 'top' ? 0 : build.log.options.lastOffset;
  build.log.hasNavigatedWithButtons = true;

  build.log
    .reset()
    .setOffset(updatedOffset)
    .fetch()
    .always((data, textStatus, jqxhr) => {
      build.log.positionChange = position;
      build.log.nextOffset = data.nextOffset;

      if (position === 'top') {
        build.log.currentPage = 0;
        build.log.endOfLogLoaded = false;
      }

      else {
        build.log.currentPage = build.log.options.logPages;
      }

      updateStore(build, build.log, data, textStatus, jqxhr)
  });
}

function updateStore(build, log, data, textStatus, jqxhr) {
  if (jqxhr.responseJSON === undefined || textStatus !== 'success') {
    BuildActions.loadBuildSuccess({
      build: build.data,
      log: [{ text: data.responseText}],
      fetchingLog: false
    });
    return;
  }

  BuildActions.loadBuildSuccess({
    build: build.data,
    log: log.logLines,
    fetchingLog: false,
    currentOffset: log.options.offset,
    currrentOffsetLine: log.currrentOffsetLine,
    lastOffsetLine: build.log.lastOffsetLine,
    positionChange: build.log.positionChange
  });
}



export default BuildActions;
