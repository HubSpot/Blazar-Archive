import Resource from '../services/ResourceProvider';
import { fromJS } from 'immutable';
import humanizeDuration from 'humanize-duration';
import $ from 'jquery';

import { getUsernameFromCookie } from '../components/Helpers.js';
import { getBranchBuildPath } from '../utils/blazarPaths';

function _parse(params, resp) {
  const builds = resp.map((build) => {
    build.blazarPath = getBranchBuildPath(params.branchId, build.buildNumber);
    if (build.endTimestamp && build.startTimestamp) {
      build.duration = humanizeDuration(build.endTimestamp - build.startTimestamp, {round: true});
    }
    return build;
  });

  return fromJS(builds);
}

function _parseModules(resp) {
  return resp.filter((module) => {
    return module.active;
  });
}

function fetchBranchBuildHistory(params, cb) {
  const exclusionOpts = {
    property: [
      '!buildTrigger',
      '!buildOptions',
      '!dependencyGraph',
      '!commitInfo.newCommits',
      '!commitInfo.previous',
      '!commitInfo.truncated',
      '!commitInfo.current.author',
      '!commitInfo.current.committer',
      '!commitInfo.current.modified',
      '!commitInfo.current.added',
      '!commitInfo.current.removed'
    ]
  };

  const branchBuildHistoryPromise = new Resource({
    url: `${window.config.apiRoot}/builds/history/branch/${params.branchId}`,
    type: 'GET',
    data: $.param(exclusionOpts, true)
  }).send();

  return branchBuildHistoryPromise.then((resp) => {
    cb(_parse(params, resp));
  });
}

function fetchBranchInfo(params) {
  return new Resource({
    url: `${window.config.apiRoot}/branches/${params.branchId}`,
    type: 'GET'
  }).send();
}

function fetchBranchModules(params, cb) {
  const branchModulesPromise = new Resource({
    url: `${window.config.apiRoot}/branches/${params.branchId}/modules`,
    type: 'GET'
  }).send();

  branchModulesPromise.then((resp) => {
    cb(_parseModules(resp));
  });
}

function fetchMalformedFiles(params, cb) {
  const malformedFilesPromise = new Resource({
    url: `${window.config.apiRoot}/branches/${params.branchId}/malformedFiles`,
    type: 'GET'
  }).send();

  malformedFilesPromise.then((resp) => {
    cb(resp);
  });
}

function _generateBuildModuleJsonBody(moduleIds, downstreamModules, resetCache) {
  return JSON.stringify({
    moduleIds,
    buildDownstreams: downstreamModules ? 'WITHIN_REPOSITORY' : 'NONE',
    resetCaches: resetCache
  });
}

function triggerBuild(branchId, moduleIds, downstreamModules, resetCache) {
  if (moduleIds === null) {
    moduleIds = [];
  }
  const username = getUsernameFromCookie() ? `username=${getUsernameFromCookie()}` : '';
  return new Resource({
    url: `${window.config.apiRoot}/branches/builds/branch/${branchId}?${username}`,
    type: 'POST',
    contentType: 'application/json',
    data: _generateBuildModuleJsonBody(moduleIds, downstreamModules, resetCache)
  }).send();
}

export default {
  fetchBranchBuildHistory,
  fetchBranchInfo,
  fetchBranchModules,
  fetchMalformedFiles,
  triggerBuild
};
