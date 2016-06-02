/*global config*/
import Resource from '../services/ResourceProvider';
import { fromJS } from 'immutable';
import { getUsernameFromCookie } from '../components/Helpers.js';

function _generateBuildModuleJsonBody(moduleIds, downstreamModules, resetCache) {
  return JSON.stringify({
    moduleIds: moduleIds,
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
    contentType: 'application/json',
    data: _generateBuildModuleJsonBody(moduleIds, 'NONE', resetCache)
  }).send();

  buildPromise.then((resp) => {
    cb(false, resp);
  }, (error) => {
    console.warn(error);
    cb('Error triggering build. Check your console for more detail.');
  });
}

function getInterProjectBuild(interProjectBuildId, cb) {
  const interProjectBuildPromise = new Resource({
    url: `${config.apiRoot}/inter-project-builds/${interProjectBuildId}`,
    type: 'GET',
    contentType: 'application/json',
  }).send();

  interProjectBuildPromise.then((resp) => {
    cb(resp);
  });
}

function getInterProjectBuildMappings(interProjectBuildId, cb) {
  const interProjectBuildMappingsPromise = new Resource({
    url: `${config.apiRoot}/inter-project-builds/${interProjectBuildId}/mappings`,
    type: 'GET',
    contentType: 'application/json',
  }).send();

  interProjectBuildMappingsPromise.then((resp) => {
    cb(resp);
  });
}

export default {
  triggerInterProjectBuild: triggerInterProjectBuild,
  getInterProjectBuild: getInterProjectBuild,
  getInterProjectBuildMappings: getInterProjectBuildMappings
};
