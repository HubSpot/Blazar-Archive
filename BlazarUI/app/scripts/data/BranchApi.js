/*global config*/
import Resource from '../services/ResourceProvider';
import { fromJS } from 'immutable';
import humanizeDuration from 'humanize-duration';

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
  return resp;
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

function _generateBuildModuleJsonBody(modules, downstreamModules) {
  const moduleIds = modules.map((module) => {
    return module.value;
  });

  return JSON.stringify({
    moduleIds: moduleIds, 
    buildDownstreams: downstreamModules
  });
}

function triggerBuild(params, modules, downstreamModules, cb) {
  const buildPromise = new Resource({
    url: `${config.apiRoot}/branches/builds/branch/${params.branchId}`,
    type: 'POST',
    contentType: 'application/json',
    data: _generateBuildModuleJsonBody(modules, downstreamModules)
  }).send();

  buildPromise.then((resp) => {
    cb(false, resp);
  }, (error) => {
    console.warn(error);
    cb('Error triggering build. Check your console for more detail.');
  });
}

export default {
  fetchBranchBuildHistory: fetchBranchBuildHistory,
  fetchBranchInfo: fetchBranchInfo,
  fetchBranchModules: fetchBranchModules,
  fetchMalformedFiles: fetchMalformedFiles,
  triggerBuild: triggerBuild
};