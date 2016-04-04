/*global config*/
import Resource from '../services/ResourceProvider';
import { findWhere } from 'underscore';

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
      return cb(resp);
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

    return moduleBuildsPromise.then((resp) => {
      console.log(resp);
      return cb(resp);
    });
  })
}

function fetchModuleBuilds(params, cb) {
  return _fetchBranchBuildHistory(params).then((resp) => {
    const repoBuild = findWhere(resp, {buildNumber: parseInt(params.buildNumber, 10)});
    const moduleBuildsPromise = new Resource({
      url: `${config.apiRoot}/branches/builds/${repoBuild.id}/modules`,
      type: 'GET'
    }).send();

    _fetchModuleNames(params).then((resp) => {
      console.log("modules: ", resp);
    });

    return moduleBuildsPromise.then((resp) => {
      return cb(resp);
    });
  })
}

export default {
  fetchRepoBuild: fetchRepoBuild,
  fetchModuleBuilds: fetchModuleBuilds
};