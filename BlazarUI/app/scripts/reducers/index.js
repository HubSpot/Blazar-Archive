import { combineReducers } from 'redux';
import branchState from './branchState';
import repo from './repo';
import branch from './branch';
import starredBranches from './starredBranches';
import moduleBuildHistoriesByModuleId from './moduleBuildHistoriesByModuleId';

export default combineReducers({
  branchState,
  repo,
  branch,
  starredBranches,
  moduleBuildHistoriesByModuleId
});
