import ActionTypes from './ActionTypes';
import BranchStateApi from '../data/BranchStateApi';
import { loadModuleBuildHistory } from './moduleBuildHistoryActions';

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

export const selectModule = (moduleId) => {
  return (dispatch) => {
    dispatch({
      type: ActionTypes.SELECT_MODULE,
      payload: moduleId
    });
    dispatch(loadModuleBuildHistory(moduleId));
  };
};

export const deselectModule = () => ({
  type: ActionTypes.DESELECT_MODULE
});
