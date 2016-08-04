/* global config*/
import Resource from '../services/ResourceProvider';
import { fromJS } from 'immutable';
import { getUsernameFromCookie } from '../components/Helpers.js';

function _generateBuildModuleJsonBody(moduleIds, downstreamModules, resetCache) {
  return JSON.stringify({
    moduleIds,
    buildDownstreams: downstreamModules,
    resetCaches: resetCache
  });
}

function triggerInterProjectBuild(moduleIds, resetCache, cb) {
  if (moduleIds === null) {
    moduleIds = [];
  }
  const username = getUsernameFromCookie() ? `username=${getUsernameFromCookie()}` : '';
  const buildPromise = new Resource({
    url: `${config.apiRoot}/inter-project-builds?${username}`,
    type: 'POST',
    data: _generateBuildModuleJsonBody(moduleIds, 'NONE', resetCache)
  }).send();

  buildPromise.then((resp) => {
    cb(false, resp);
  }, (error) => {
    console.warn(error);
    cb('Error triggering build. Check your console for more detail.');
  });
}

function getUpAndDownstreamModules(repoBuildId, cb) {
  const upAndDownstreamModulesPromise = new Resource({
    url: `${config.apiRoot}/inter-project-builds/repository-build/${repoBuildId}/up-and-downstreams`
  }).send();

  upAndDownstreamModulesPromise.then((resp) => {
    cb(resp);
  });
}

export default {
  triggerInterProjectBuild,
  getUpAndDownstreamModules
};
