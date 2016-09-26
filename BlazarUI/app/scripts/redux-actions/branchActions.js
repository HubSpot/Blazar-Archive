import ActionTypes from './ActionTypes';
import BranchApi from '../data/BranchApi';
import BranchInfo from '../data/records/BranchInfo';

export const loadBranchInfo = (branchId) => {
  return (dispatch) => {
    dispatch({
      type: ActionTypes.REQUEST_BRANCH_INFO,
      payload: branchId
    });

    BranchApi.fetchBranchInfo({branchId}).then((branchInfo) => {
      dispatch({
        type: ActionTypes.RECEIVE_BRANCH_INFO,
        payload: new BranchInfo(branchInfo)
      });
    }, (error) => {
      dispatch({
        type: ActionTypes.RECEIVE_BRANCH_INFO,
        error: true,
        payload: error
      });
    });
  };
};
