import $ from 'jquery';
import {has} from 'underscore';
import humanizeDuration from 'humanize-duration';
import PollingProvider from '../services/PollingProvider';
import store from '../reduxStore';
import { getRepoPath, getBranchBuildPath, getBranchStatePath } from '../utils/blazarPaths';

function _groupBuilds(builds) {
  const starredBranchIds = store.getState().starredBranches;
  return {
    all: builds,
    starred: builds.filter((build) => starredBranchIds.has(build.gitInfo.id)),
    building: builds.filter((build) => has(build, 'inProgressBuild'))
  };
}

function _parse(data) {
  const parsed = data.map((item) => {
    const {
      gitInfo,
      lastBuild,
      inProgressBuild
    } = item;

    const branchId = gitInfo.id;

    if (has(item, 'inProgressBuild')) {
      item.inProgressBuild.duration = humanizeDuration(Date.now() - item.inProgressBuild.startTimestamp, {round: true});
      item.inProgressBuild.blazarPath = getBranchBuildPath(branchId, inProgressBuild.buildNumber);
    }

    if (has(item, 'lastBuild')) {
      item.lastBuild.duration = humanizeDuration(item.lastBuild.endTimestamp - item.lastBuild.startTimestamp, {round: true});
      item.lastBuild.blazarPath = getBranchBuildPath(branchId, lastBuild.buildNumber);
    }

    item.gitInfo.blazarRepositoryPath = getRepoPath(gitInfo.repository);
    item.gitInfo.blazarBranchPath = getBranchStatePath(branchId);

    return item;
  });

  return parsed;
}

function fetchBuilds(extraData, cb) {
  if (this.buildsPoller) {
    this.buildsPoller.disconnect();
    this.buildsPoller = undefined;
  }

  let exclusionOpts = {
    property: [
      '!pendingBuild.buildOptions',
      '!inProgressBuild.buildOptions',
      '!lastBuild.buildOptions'
    ]
  };

  if (!extraData) {
    exclusionOpts = {
      property: [
        '!pendingBuild.commitInfo',
        '!inProgressBuild.commitInfo',
        '!lastBuild.commitInfo',
        '!pendingBuild.dependencyGraph',
        '!inProgressBuild.dependencyGraph',
        '!lastBuild.dependencyGraph',
        '!pendingBuild.buildTrigger',
        '!inProgressBuild.buildTrigger',
        '!lastBuild.buildTrigger',
        '!pendingBuild.buildOptions',
        '!inProgressBuild.buildOptions',
        '!lastBuild.buildOptions'
      ]
    };
  }

  this.buildsPoller = new PollingProvider({
    url: `${window.config.apiRoot}/branches/state`,
    type: 'GET',
    dataType: 'json',
    data: $.param(exclusionOpts, true)
  });

  this.buildsPoller.poll((err, resp) => {
    if (err) {
      cb(err);
      return;
    }

    cb(err, _groupBuilds(_parse(resp)));
  });
}

function stopPolling() {
  if (!this.buildsPoller) {
    return;
  }

  this.buildsPoller.disconnect();
}

export default {
  fetchBuilds,
  stopPolling
};
