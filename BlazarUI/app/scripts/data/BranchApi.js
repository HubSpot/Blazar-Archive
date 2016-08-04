/* global config*/
import Resource from '../services/ResourceProvider';
import { fromJS } from 'immutable';
import humanizeDuration from 'humanize-duration';
import { getUsernameFromCookie } from '../components/Helpers.js';

function _parse(params, resp) {
  const builds = resp.map((build) => {
    build.blazarPath = `/builds/branch/${params.branchId}/build/${build.buildNumber}`;
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
  const branchBuildHistoryPromise = new Resource({
    url: `${config.apiRoot}/builds/history/branch/${params.branchId}`,
    type: 'GET'
  }).send();

  return branchBuildHistoryPromise.then((resp) => {
    cb(_parse(params, resp));
  });
}

function fetchBranchInfo(params, cb) {
  const branchInfoPromise = new Resource({
    url: `${config.apiRoot}/branches/${params.branchId}`,
    type: 'GET'
  }).send();

  return branchInfoPromise.then((resp) => {
    cb(resp);
  });
}

function fetchBranchModules(params, cb) {
  const branchModulesPromise = new Resource({
    url: `${config.apiRoot}/branches/${params.branchId}/modules`,
    type: 'GET'
  }).send();

  branchModulesPromise.then((resp) => {
    cb(_parseModules(resp));
  });
}

function fetchMalformedFiles(params, cb) {
  const malformedFilesPromise = new Resource({
    url: `${config.apiRoot}/branches/${params.branchId}/malformedFiles`,
    type: 'GET'
  }).send();

  malformedFilesPromise.then((resp) => {
    cb(resp);
  });
}

function _generateBuildModuleJsonBody(moduleIds, downstreamModules, resetCache) {
  return JSON.stringify({
    moduleIds,
    buildDownstreams: downstreamModules,
    resetCaches: resetCache
  });
}

function triggerBuild(params, moduleIds, downstreamModules, resetCache, cb) {
  if (moduleIds === null) {
    moduleIds = [];
  }
  const username = getUsernameFromCookie() ? `username=${getUsernameFromCookie()}` : '';
  const buildPromise = new Resource({
    url: `${config.apiRoot}/branches/builds/branch/${params.branchId}?${username}`,
    type: 'POST',
    contentType: 'application/json',
    data: _generateBuildModuleJsonBody(moduleIds, downstreamModules, resetCache)
  }).send();

  buildPromise.then((resp) => {
    cb(false, resp);
  }, (error) => {
    console.warn(error);
    cb('Error triggering build. Check your console for more detail.');
  });
}

export default {
  fetchBranchBuildHistory,
  fetchBranchInfo,
  fetchBranchModules,
  fetchMalformedFiles,
  triggerBuild
};
