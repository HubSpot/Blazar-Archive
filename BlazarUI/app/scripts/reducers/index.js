import { combineReducers } from 'redux';
import branchState from './branchState';
import repo from './repo';
import branch from './branch';

export default combineReducers({
  branchState,
  repo,
  branch
});
