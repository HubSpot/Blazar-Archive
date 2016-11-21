import $ from 'jquery';
import Q from 'q';
import { findWhere, map, extend, max, contains } from 'underscore';
import humanizeDuration from 'humanize-duration';

import { getErrorMessage } from './apiUtils';
import Resource from '../services/ResourceProvider';

function _parse(resp) {
  if (resp.startTimestamp && resp.endTimestamp) {
    resp.duration = humanizeDuration(resp.endTimestamp - resp.startTimestamp, {round: true});
  }

  return resp;
}

function _fetchModuleNames(branchId) {
  return new Resource({
    url: `${window.config.apiRoot}/branches/${branchId}/modules`,
    type: 'GET'
  }).send();
}

function _fetchBranchBuildHistory(branchId) {
  const inclusionOpts = {
    property: [
      'buildNumber',
      'id',
      'state'
    ]
  };

  return new Resource({
    url: `${window.config.apiRoot}/builds/history/branch/${branchId}`,
    type: 'GET',
    data: $.param(inclusionOpts, true)
  }).send();
}

function _getRepoBuildByParam(buildNumber, repoBuilds) {
  if (buildNumber === 'latest') {
    const notStartedBuilds = repoBuilds.filter((build) => {
      return !contains(['QUEUED', 'LAUNCHING', 'WAITING_FOR_BUILD_SLOT', 'WAITING_FOR_UPSTREAM_BUILD'], build.state);
    });

    return max(notStartedBuilds, (build) => build.buildNumber);
  }

  return findWhere(repoBuilds, {buildNumber: parseInt(buildNumber, 10)});
}

function _fetchRepoBuildId(branchId, buildNumber) {
  return Q(_fetchBranchBuildHistory(branchId)).then((resp) => {
    const repoBuild = _getRepoBuildByParam(buildNumber, resp);

    if (!repoBuild) {
      return Q.reject(`Build #${buildNumber} does not exist for this branch.`);
    }

    return repoBuild.id;
  });
}

function fetchRepoBuildById(repoBuildId) {
  const repoBuildPromise = new Resource({
    url: `${window.config.apiRoot}/branches/builds/${repoBuildId}`,
    type: 'GET'
  }).send();

  return Q(repoBuildPromise).then(
    (repoBuilds) => _parse(repoBuilds),
    (error) => Q.reject(`Unable to fetch branch build: ${getErrorMessage(error)}`)
  );
}

function fetchRepoBuild(params) {
  const {branchId, buildNumber} = params;
  return _fetchRepoBuildId(branchId, buildNumber)
    .then(fetchRepoBuildById);
}

function fetchModuleBuildsById(branchId, repoBuildId, buildNumber) {
  const moduleInfoPromise = _fetchModuleNames(branchId);
  const moduleBuildsPromise = new Resource({
    url: `${window.config.apiRoot}/branches/builds/${repoBuildId}/modules`,
    type: 'GET'
  }).send();

  return Q.spread([moduleInfoPromise, moduleBuildsPromise],
    (moduleInfos, moduleBuilds) => {
      const moduleBuildsWithNames = map(moduleBuilds, (build) => {
        const moduleInfo = findWhere(moduleInfos, {id: build.moduleId});
        const moduleInfoExtended = {
          name: moduleInfo.name,
          blazarPath: `/builds/branch/${branchId}/build/${buildNumber}/module/${moduleInfo.name}`
        };

        return extend(build, moduleInfoExtended);
      });

      return moduleBuildsWithNames;
    });
}

function fetchModuleBuilds(params) {
  const {branchId, buildNumber} = params;
  return _fetchRepoBuildId(branchId, buildNumber).then((repoBuildId) => {
    return fetchModuleBuildsById(branchId, repoBuildId, buildNumber);
  });
}

function cancelBuild(repoBuildId) {
  const cancelPromise = new Resource({
    url: `${window.config.apiRoot}/branches/builds/${repoBuildId}/cancel`,
    type: 'POST'
  }).send();

  cancelPromise.error((error) => {
    console.warn(error); // TODO: be better
  });
}

export default {
  fetchRepoBuild,
  fetchRepoBuildById,
  fetchModuleBuilds,
  fetchModuleBuildsById,
  cancelBuild
};
