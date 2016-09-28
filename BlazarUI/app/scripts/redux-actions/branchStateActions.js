import ActionTypes from './ActionTypes';
import BranchStateApi from '../data/BranchStateApi';
import { loadModuleBuildHistory } from './moduleBuildHistoryActions';

let pollingTimeoutId = null;

export const loadBranchModuleStates = (branchId) => {
  return (dispatch) => {
    return BranchStateApi.fetchModuleStates(branchId).then((moduleStates) => {
      dispatch({
        type: ActionTypes.RECEIVE_MODULE_STATES,
        payload: moduleStates
      });
    });
  };
};

const _pollBranchModuleStates = (branchId) => {
  return (dispatch, getState) => {
    dispatch(loadBranchModuleStates(branchId)).then(() => {
      const state = getState().branchState;
      if (state.get('isPolling') && state.get('branchId') === branchId) {
        pollingTimeoutId = setTimeout(() => {
          dispatch(_pollBranchModuleStates(branchId));
        }, window.config.moduleStateRefresh);
      }
    });
  };
};

export const pollBranchModuleStates = (branchId) => {
  clearTimeout(pollingTimeoutId);
  return (dispatch) => {
    dispatch({
      type: ActionTypes.START_POLLING_MODULE_STATES,
      payload: branchId
    });
    dispatch(_pollBranchModuleStates(branchId));
  };
};

export const stopPollingBranchModuleStates = () => ({
  type: ActionTypes.STOP_POLLING_MODULE_STATES
});

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

export default {
  loadBranchModuleStates,
  pollBranchModuleStates,
  stopPollingBranchModuleStates,
  selectModule,
  deselectModule
};
