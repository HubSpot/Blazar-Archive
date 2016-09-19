import Resource from '../services/ResourceProvider';
import mockBranchState from '../data/mockBranchState';

const mock = false;

function fetchModuleStates(branchId) {
  return new Resource({
    url: `${window.config.apiRoot}/branches/state/${branchId}/modules`,
  }).send();
}

function mockFetchModuleStates() {
  return Promise.resolve(mockBranchState);
}

function fetchModuleBuildHistory(moduleId) {
  return new Resource({
    url: `${window.config.apiRoot}/builds/history/module/${moduleId}?property=!buildConfig&property=!resolvedConfig`,
  }).send();
}

export default {
  fetchModuleStates: mock ? mockFetchModuleStates : fetchModuleStates,
  fetchModuleBuildHistory
};
