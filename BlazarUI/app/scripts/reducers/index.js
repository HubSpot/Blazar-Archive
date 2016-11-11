import { combineReducers } from 'redux';
import branchState from './branchState';
import branches from './branches';
import repositories from './repositories';
import starredBranches from './starredBranches';
import moduleBuildHistoriesByModuleId from './moduleBuildHistoriesByModuleId';
import buildBranchForm from './buildBranchForm';
import dismissedBetaNotifications from './dismissedBetaNotifications';

export default combineReducers({
  branchState,
  branches,
  repositories,
  starredBranches,
  moduleBuildHistoriesByModuleId,
  buildBranchForm,
  dismissedBetaNotifications
});
