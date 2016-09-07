import ActionTypes from './ActionTypes';
import BranchStateApi from '../data/BranchStateApi';

export const loadBranchModuleStates = (branchId) => {
  return (dispatch) => {
    dispatch({type: ActionTypes.REQUEST_MODULE_STATES});

    BranchStateApi.fetchModuleStates(branchId).then((moduleStates) => {
      dispatch({
        type: ActionTypes.RECEIVE_MODULE_STATES,
        payload: moduleStates
      });
    });
  };
};

export const selectModule = (moduleId) => ({
  type: ActionTypes.SELECT_MODULE,
  payload: moduleId
});
