/*global config*/
import Reflux from 'reflux';
import {find, has} from 'underscore';
import {buildIsOnDeck} from '../components/Helpers';

import Build from '../models/Build';
import Log from '../models/Log';
import LogSize from '../models/LogSize';
import BuildTrigger from '../models/BuildTrigger';
import BranchDefinition from '../models/BranchDefinition';
import BranchModules from '../collections/BranchModules';

import BuildStates from '../constants/BuildStates';
import BuildHistoryActions from '../actions/buildHistoryActions';

// map to keep track of builds being viewed
// in the event that we navigate to other builds
let builds = {};
// temporary storage for a build
// we are loading details for
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
  'pageLog',
  'fetchEndOfLog',
  'fetchStartOfLog',
  'shouldPoll'
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

BuildActions.shouldPoll = function(moduleId, state) {
  builds[moduleId].shouldPoll = state;
};

// Scrolling up or down log
BuildActions.pageLog = function(moduleId, direction) {  
  const isActive = builds[moduleId].data.build.state === BuildStates.IN_PROGRESS;
  handleScroll(builds[moduleId], direction, isActive);
};

BuildActions.fetchStartOfLog = function(moduleId, options={}) {
  builds[moduleId].shouldPoll = false;

  if (options.position) {
    builds[moduleId].log.positionChange = options.position;
  }

  resetBuild({
    build: builds[moduleId],
    offset: 0,
    position: 'top'
  });
}

BuildActions.fetchEndOfLog = function(moduleId, options={}) {
  const build = builds[moduleId];
  const logSizePromise = getLogSize();
  const buildInProgress = build.data.build.state === BuildStates.IN_PROGRESS;
  
  if (options.poll && buildInProgress) {
    build.shouldPoll = true;
  }
  
  logSizePromise.done((size) => {
    build.log.reset().setOffset(getLastOffset(size))

    if (options.position) {
      build.log.positionChange = options.position;
      build.log.hasNavigatedWithButtons = true;
    }

    if (buildInProgress) {
      processInProgressBuild(build);  
    }
    else {
      resetBuild({
        build: build,
        offset: getLastOffset(size),
        position: 'bottom'
      });
    }
    
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
    BuildActions.loadBuildError('Error retrieving build. Build does not exist or no longer exists.');
  });

  return buildPromise;
}

function getLogSize() {
  const build = builds[requestedBuild.gitInfo.moduleId]
  
  if (build.data.build.state === BuildStates.LAUNCHING || build.data.build.state === BuildStates.QUEUED) {
    return;
  }
  
  const logSize = new LogSize({
    buildNumber: build.data.build.id
  });

  const sizePromise = logSize.fetch();
  
  sizePromise.done((size) => {
    requestedBuild.logSize = size;
    build.log = createLogModel(build, requestedBuild.logSize);
  });
  
  sizePromise.error((err) => {
    console.warn(err);
    BuildActions.loadBuildSuccess({
      error: {responseText: `Error loading log, we don't have the log size. View your console for more detail.` },
      build: build.data
    });
    
  });

  return sizePromise;
}

function processBuild() {
  const buildToProcess = builds[requestedBuild.gitInfo.moduleId];

  // To do: find out why we dont have state at this point
  if (!has(buildToProcess.data.build, 'state') || !buildToProcess.isActive) {
    return;
  }

  // States: Launching, Queued
  if (buildIsOnDeck(buildToProcess.data.build.state)) {
    BuildActions.loadBuildSuccess({
      build: buildToProcess.data
    });
    return;
  }
  // State: In Progress
  if (buildToProcess.data.build.state === BuildStates.IN_PROGRESS) {
    buildToProcess.shouldPoll = true;
    processInProgressBuild(buildToProcess);
  }
  // State: Failed, Cancelled
  else {
    processInactiveBuild(buildToProcess);
  }
}

function processInactiveBuild(build) {
  const log = build.log;
  const logPromise = log.fetch();
  logPromise.always((data, textStatus, jqxhr) => {
    build.log.nextOffset = data.nextOffset;
    updateStore(build, log, data, textStatus, jqxhr);
  });  
}

function processInProgressBuild(build) {
  if (!build.isActive || !build.shouldPoll) {
    return;
  }
  // user has paged up, stop polling
  if (build.log.navigationPosition === 'up') {
    return;
  }

  const logPromise = build.log.fetch();

  logPromise.always( (data, textStatus, jqxhr) => {
    build.log.nextOffset = data.nextOffset;
    build.log.setOffset(data.nextOffset);

    // reached end of log
    if (data.nextOffset === -1) {
      // get latest build detail when log fetching is complete
      // so we can update status section at top of build page
      const lastBuild = new Build(requestedBuild.gitInfo);
      const buildPromise = lastBuild.fetch();
  
      buildPromise.done(() => {
        BuildActions.loadBuildSuccess({
          build: lastBuild.data,
          log: build.log,
          fetchingLog: false
        });
      });
    }

    // still building
    else {
      BuildActions.loadBuildSuccess({
        build: build.data,
        log: build.log,
        fetchingLog: true
      });
      
      setTimeout(() => {
        processInProgressBuild(build)
      }, config.activeBuildRefresh);
    }
  });  
}

//
// Shared Methods Across Build States
//
function getLastOffset(size) {
  return Math.max(size - config.offsetLength, 0);
}

function createLogModel(build, size) {
  const inProgress = build.data.build.state === BuildStates.IN_PROGRESS;
  // const backupOffset = inProgress ? 1000 : config.offsetLength;
  const maxOffset = getLastOffset(size);
  
  const settings = {
    buildState: build.data.build.state,
    buildNumber: build.data.build.id,
    logSize: size,
    logPages: Math.ceil(maxOffset / config.offsetLength),
    startingOffset: maxOffset,
    lastOffset: maxOffset,
    offset: maxOffset,
    offsetLength: config.offsetLength
  }
  
  return new Log(settings);
}

// fetch previous/next offsets when scrolling up/down log
function handleScroll(build, direction, isActive) {
  build.log.hasNavigatedWithButtons = false;
  build.log.direction = direction;
  build.log.positionChange = undefined;
  
  // Log size is smaller than one offsetLength so nothing more to fetch
  if (build.log.options.logSize < config.offsetLength) {
    return;
  }

  if (direction === 'down') {
    // if we are at the end of the log, started at the end and since scrolled up,
    // or our log is less than one offset length
    if (build.log.endOfLogLoaded || build.log.options.logSize < config.offsetLength + 1) {
      return;
    }
  }

  else if (direction === 'up') {
    // If we made it to the top, dont fetch anything
    if (build.log.startOfLogLoaded) {
      return;
    }
  }

  build.log.pageLog(direction).fetch().always((data, textStatus, jqxhr) => {
    updateStore(build, build.log, data, textStatus, jqxhr)
  });
}

// Pass a build and offset to set
function resetBuild(options) {

  const {
    build, 
    offset, 
    position
  } = options;

  build.log.endOfLogLoaded = false;
  build.log.startOfLogLoaded = false;

  build.log
    .reset()
    .setOffset(offset)
    .fetch()
    .always((data, textStatus, jqxhr) => {
      build.log.positionChange = position;
      build.log.nextOffset = data.nextOffset;
      build.log.previousOffset = offset;
      updateStore(build, build.log, data, textStatus, jqxhr)
  });
}

function updateStore(build, log, data, textStatus, jqxhr) {
  BuildActions.loadBuildSuccess({
    build: build.data,
    log: log,
    fetchingLog: false
  });
}


export default BuildActions;
