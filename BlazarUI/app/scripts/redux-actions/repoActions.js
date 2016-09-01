import ActionTypes from './ActionTypes';
import RepoApi from '../data/RepoApi';

export const loadBranches = (repoId) => {
  return (dispatch) => {
    dispatch({
      type: ActionTypes.REQUEST_BRANCHES_IN_REPO,
      payload: repoId
    });

    RepoApi.fetchBranchesInRepo(repoId, (branchesList) => {
      dispatch({
        type: ActionTypes.RECEIVE_BRANCHES_IN_REPO,
        payload: branchesList
      });
    });
  };
};
