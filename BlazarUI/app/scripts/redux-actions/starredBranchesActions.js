import ActionTypes from './ActionTypes';

export const toggleStar = (branchId) => ({
  type: ActionTypes.TOGGLE_STARRED_BRANCH,
  payload: branchId
});
