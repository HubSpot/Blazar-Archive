import Immutable from 'immutable';
import { createSelector } from 'reselect';

const getSelectedBranchId = (state) => state.branchState.get('branchId');
const getBranches = (state) => state.branches;
const getRepositories = (state) => state.repositories;

export const getSelectedBranch = createSelector(
  [getSelectedBranchId, getBranches],
  (selectedBranchId, branches) => {
    return branches.get(selectedBranchId);
  }
);

export const getBranchesInRepository = createSelector(
  [getSelectedBranch, getBranches, getRepositories],
  (selectedBranch, branches, repositories) => {
    if (!selectedBranch) {
      return Immutable.Set();
    }

    const repository = repositories.get(selectedBranch.repositoryId);
    if (!repository) {
      return Immutable.Set();
    }

    return repository.get('branches').map((branchId) => branches.get(branchId));
  }
);

const getModuleStates = (state) => state.branchState.get('moduleStates');

export const getActiveModules = createSelector([getModuleStates],
  (moduleStates) => moduleStates.filter(moduleState => moduleState.getIn(['module', 'active']))
);

export const getInactiveModules = createSelector([getModuleStates],
  (moduleStates) => moduleStates.filter(moduleState => !moduleState.getIn(['module', 'active']))
);
