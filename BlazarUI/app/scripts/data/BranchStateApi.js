import Resource from '../services/ResourceProvider';
import mockBranchState from '../data/mockBranchState';

const mock = true;

function fetchModuleStates(branchId) {
  return new Resource({
    url: `${window.config.apiRoot}/branches/state/${branchId}/modules`,
    type: 'GET'
  }).send();
}

function mockFetchModuleStates() {
  return Promise.resolve(mockBranchState);
}

export default {
  fetchModuleStates: mock ? mockFetchModuleStates : fetchModuleStates
};
