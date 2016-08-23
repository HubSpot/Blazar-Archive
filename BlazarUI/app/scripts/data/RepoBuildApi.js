import Resource from '../services/ResourceProvider';
import Q from 'q';
import { findWhere, map, extend, max, contains } from 'underscore';
import humanizeDuration from 'humanize-duration';
import $ from 'jquery';

function _parse(resp) {
  if (resp.startTimestamp && resp.endTimestamp) {
    resp.duration = humanizeDuration(resp.endTimestamp - resp.startTimestamp, {round: true});
  }

  return resp;
}

function _fetchModuleNames(params) {
  const moduleNamesPromise = new Resource({
    url: `${window.config.apiRoot}/branches/${params.branchId}/modules`,
    type: 'GET'
  }).send();

  return moduleNamesPromise;
}

function _fetchModuleNamesById(branchId) {
  const moduleNamesPromise = new Resource({
    url: `${window.config.apiRoot}/branches/${branchId}/modules`,
    type: 'GET'
  }).send();

  return moduleNamesPromise;
}

function _fetchBranchBuildHistory(params) {
  const inclusionOpts = {
    property: [
      'buildNumber',
      'id',
      'state'
    ]
  };

  const branchBuildHistoryPromise = new Resource({
    url: `${window.config.apiRoot}/builds/history/branch/${params.branchId}`,
    type: 'GET',
    data: $.param(inclusionOpts, true)
  }).send();

  return branchBuildHistoryPromise;
}

function _getRepoBuildByParam(params, resp) {
  if (params.buildNumber === 'latest') {
    resp = resp.filter((build) => {
      return !contains(['QUEUED', 'LAUNCHING', 'WAITING_FOR_BUILD_SLOT', 'WAITING_FOR_UPSTREAM_BUILD'], build.state);
    });
    return max(resp, (build) => {
      return build.buildNumber;
    });
  }

  return findWhere(resp, {buildNumber: parseInt(params.buildNumber, 10)});
}

function fetchRepoBuild(params, cb) {
  return _fetchBranchBuildHistory(params).then((resp) => {
    const repoBuild = _getRepoBuildByParam(params, resp);
    const repoBuildPromise = new Resource({
      url: `${window.config.apiRoot}/branches/builds/${repoBuild.id}`,
      type: 'GET'
    }).send();

    return repoBuildPromise.then((repoBuilds) => {
      return cb(_parse(repoBuilds));
    });
  });
}

function fetchRepoBuildById(repoBuildId, cb) {
  const repoBuildPromise = new Resource({
    url: `${window.config.apiRoot}/branches/builds/${repoBuildId}`,
    type: 'GET'
  }).send();

  return repoBuildPromise.then((resp) => {
    return cb(_parse(resp));
  });
}

function fetchModuleBuilds(params, cb) {
  return _fetchBranchBuildHistory(params).then((resp) => {
    const repoBuild = _getRepoBuildByParam(params, resp);
    const moduleBuildsPromise = new Resource({
      url: `${window.config.apiRoot}/branches/builds/${repoBuild.id}/modules`,
      type: 'GET'
    }).send();
    const moduleInfoPromise = _fetchModuleNames(params);

    Q.spread([moduleInfoPromise, moduleBuildsPromise],
      (moduleInfos, moduleBuilds) => {
        const moduleBuildsWithNames = map(moduleBuilds, (build) => {
          const moduleInfo = findWhere(moduleInfos, {id: build.moduleId});
          const moduleInfoExtended = {
            name: moduleInfo.name,
            blazarPath: `/builds/branch/${params.branchId}/build/${repoBuild.buildNumber}/module/${moduleInfo.name}`
          };

          return extend(build, moduleInfoExtended);
        });

        cb(moduleBuildsWithNames);
      });
  });
}

function fetchModuleBuildsById(branchId, repoBuildId, buildNumber, cb) {
  const moduleBuildsPromise = new Resource({
    url: `${window.config.apiRoot}/branches/builds/${repoBuildId}/modules`,
    type: 'GET'
  }).send();
  const moduleInfoPromise = _fetchModuleNamesById(branchId);

  Q.spread([moduleInfoPromise, moduleBuildsPromise],
    (moduleInfos, moduleBuilds) => {
      const moduleBuildsWithNames = map(moduleBuilds, (build) => {
        const moduleInfo = findWhere(moduleInfos, {id: build.moduleId});
        const moduleInfoExtended = {
          name: moduleInfo.name,
          blazarPath: `/builds/branch/${branchId}/build/${buildNumber}/module/${moduleInfo.name}`
        };

        return extend(build, moduleInfoExtended);
      });

      cb(moduleBuildsWithNames);
    });
}

function cancelBuild(params) {
  return _fetchBranchBuildHistory(params).then((resp) => {
    const repoBuild = findWhere(resp, {buildNumber: parseInt(params.buildNumber, 10)});
    const cancelPromise = new Resource({
      url: `${window.config.apiRoot}/branches/builds/${repoBuild.id}/cancel`,
      type: 'POST'
    }).send();

    cancelPromise.error((error) => {
      console.warn(error); // TODO: be better
    });
  });
}

export default {
  fetchRepoBuild,
  fetchRepoBuildById,
  fetchModuleBuilds,
  fetchModuleBuildsById,
  cancelBuild
};
