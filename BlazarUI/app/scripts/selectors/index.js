import Immutable from 'immutable';
import { createSelector } from 'reselect';
import BranchBuildStates from '../constants/BranchBuildStates';

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

// For now, only display modules with builds on the branch state page
const getModulesWithBuilds = createSelector([getModuleStates],
  (moduleStates) => moduleStates.filter(moduleState =>
    moduleState.has('inProgressBranchBuild') ||
    moduleState.has('pendingBranchBuild') ||
    moduleState.has('lastBranchBuild')
  )
);

export const getActiveModules = createSelector([getModulesWithBuilds],
  (moduleStates) => moduleStates.filter(moduleState => moduleState.getIn(['module', 'active']))
);

export const getInactiveModules = createSelector([getModulesWithBuilds],
  (moduleStates) => moduleStates.filter(moduleState => !moduleState.getIn(['module', 'active']))
);


const getQueuedBranchBuilds = (state) => state.branchState.get('queuedBranchBuilds');
const getActiveBranchBuild = (state) => state.branchState.get('activeBranchBuild');

const getBranchBuildsForActiveModuleBuilds = createSelector([getModuleStates],
  (moduleStates) => moduleStates
    .flatMap((moduleState) => [
      moduleState.get('inProgressBranchBuild'),
      moduleState.get('pendingBranchBuild')
    ])
    .filter(branchBuilds => branchBuilds) // remove undefined values
    .toSet()
);

export const getPendingBranchBuilds = createSelector(
  [getQueuedBranchBuilds, getActiveBranchBuild, getBranchBuildsForActiveModuleBuilds],
  (queuedBranchBuilds, activeBranchBuild, branchBuildsForActiveModuleBuilds) => {
    let pendingBranchBuilds = queuedBranchBuilds;

    if (activeBranchBuild) {
      const isLaunching = activeBranchBuild.get('state') === BranchBuildStates.LAUNCHING;
      const activeBranchBuildHasNoModuleBuilds = !branchBuildsForActiveModuleBuilds.includes(activeBranchBuild);
      if (isLaunching && activeBranchBuildHasNoModuleBuilds) {
        pendingBranchBuilds = pendingBranchBuilds.push(activeBranchBuild);
      }
    }

    return pendingBranchBuilds.sortBy((branchBuild) => -branchBuild.get('buildNumber'));
  }
);
