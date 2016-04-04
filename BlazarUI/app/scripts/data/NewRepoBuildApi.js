/*global config*/
import Resource from '../services/ResourceProvider';
import Q from 'q';
import { findWhere, map, extend } from 'underscore';
import humanizeDuration from 'humanize-duration';

function _parse(resp) {
  if (resp.startTimestamp && resp.endTimestamp) {
    resp.duration = humanizeDuration(resp.endTimestamp - resp.startTimestamp, {round: true});
  }

  return resp;
}

function _fetchModuleNames(params) {
  const moduleNamesPromise = new Resource({
    url: `${config.apiRoot}/branches/${params.branchId}/modules`,
    type: 'GET'
  }).send();

  return moduleNamesPromise;
}

function _fetchBranchBuildHistory(params) {
  const branchBuildHistoryPromise = new Resource({
    url: `${config.apiRoot}/builds/history/branch/${params.branchId}`,
    type: 'GET'
  }).send();

  return branchBuildHistoryPromise;
}

function fetchRepoBuild(params, cb) {
  return _fetchBranchBuildHistory(params).then((resp) => {
    const repoBuild = findWhere(resp, {buildNumber: parseInt(params.buildNumber, 10)});
    const repoBuildPromise = new Resource({
      url: `${config.apiRoot}/branches/builds/${repoBuild.id}`,
      type: 'GET'
    }).send();

    return repoBuildPromise.then((resp) => {
      return cb(_parse(resp));
    });
  });
}

function fetchModuleBuilds(params, cb) {
  return _fetchBranchBuildHistory(params).then((resp) => {
    const repoBuild = findWhere(resp, {buildNumber: parseInt(params.buildNumber, 10)});
    const moduleBuildsPromise = new Resource({
      url: `${config.apiRoot}/branches/builds/${repoBuild.id}/modules`,
      type: 'GET'
    }).send();
    const moduleInfoPromise = _fetchModuleNames(params);

    Q.spread([moduleInfoPromise, moduleBuildsPromise],
      (moduleInfos, moduleBuilds) => {
        const moduleBuildsWithNames = map(moduleBuilds, (build) => {
          const moduleInfo = findWhere(moduleInfos, {id: build.moduleId});
          const moduleInfoExtended = {
            name: moduleInfo.name,
            blazarPath: `${config.appRoot}/builds/branch/${params.branchId}/build/${params.buildNumber}/module/${moduleInfo.name}`
          };

          return extend(build, moduleInfoExtended);
        });

        cb(moduleBuildsWithNames);
      });
  });
}

export default {
  fetchRepoBuild: fetchRepoBuild,
  fetchModuleBuilds: fetchModuleBuilds
};