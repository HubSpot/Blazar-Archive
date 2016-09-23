import Resource from '../services/ResourceProvider';

function fetchModuleStates(branchId) {
  return new Resource({
    url: `${window.config.apiRoot}/branches/state/${branchId}/modules`,
  }).send();
}

function fetchModuleBuildHistory(moduleId) {
  return new Resource({
    url: `${window.config.apiRoot}/builds/history/module/${moduleId}?property=!buildConfig&property=!resolvedConfig`,
  }).send();
}

export default {
  fetchModuleStates,
  fetchModuleBuildHistory
};
