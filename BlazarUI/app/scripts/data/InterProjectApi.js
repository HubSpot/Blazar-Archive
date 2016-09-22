import Resource from '../services/ResourceProvider';
import { getUsernameFromCookie } from '../components/Helpers.js';

function _generateBuildModuleJsonBody(moduleIds, downstreamModules, resetCache) {
  return JSON.stringify({
    moduleIds,
    buildDownstreams: downstreamModules,
    resetCaches: resetCache
  });
}

function triggerInterProjectBuild(moduleIds, resetCache) {
  if (moduleIds === null) {
    moduleIds = [];
  }
  const username = getUsernameFromCookie() ? `username=${getUsernameFromCookie()}` : '';
  return new Resource({
    url: `${window.config.apiRoot}/inter-project-builds?${username}`,
    type: 'POST',
    data: _generateBuildModuleJsonBody(moduleIds, 'NONE', resetCache)
  }).send();
}

function getUpAndDownstreamModules(repoBuildId, cb) {
  const upAndDownstreamModulesPromise = new Resource({
    url: `${window.config.apiRoot}/inter-project-builds/repository-build/${repoBuildId}/up-and-downstreams`
  }).send();

  upAndDownstreamModulesPromise.then((resp) => {
    cb(resp);
  });
}

export default {
  triggerInterProjectBuild,
  getUpAndDownstreamModules
};
