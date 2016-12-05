import ActionTypes from './ActionTypes';
import BranchStateApi from '../data/BranchStateApi';
import { loadModuleBuildHistory } from './moduleBuildHistoryActions';
import { isModuleItemSelected } from '../selectors/moduleBuildHistorySelectors';
import Q from 'q';

let pollingTimeoutId = null;

export const loadBranchStatus = (branchId) => {
  return (dispatch) => {
    return BranchStateApi.fetchBranchStatus(branchId).then((branchStatus) => {
      dispatch({
        type: ActionTypes.RECEIVE_BRANCH_STATUS,
        payload: branchStatus
      });
    }, (error) => {
      dispatch({
        type: ActionTypes.RECEIVE_BRANCH_STATUS,
        error: true,
        payload: error
      });
    });
  };
};

const shouldPollModuleBuildHistory = (getState) => {
  const state = getState();
  const selectedModuleId = state.branchState.get('selectedModuleId');
  return selectedModuleId && state.moduleBuildHistoriesByModuleId.getIn([selectedModuleId, 'page']) === 1;
};

const _pollBranchStatus = (branchId) => {
  return (dispatch, getState) => {
    const pollRequests = [dispatch(loadBranchStatus(branchId))];

    if (shouldPollModuleBuildHistory(getState)) {
      const selectedModuleId = getState().branchState.get('selectedModuleId');
      pollRequests.push(dispatch(loadModuleBuildHistory(selectedModuleId)));
    }

    Q.all(pollRequests).then(() => {
      const state = getState().branchState;
      if (state.get('isPolling') && state.get('branchId') === branchId) {
        pollingTimeoutId = setTimeout(() => {
          dispatch(_pollBranchStatus(branchId));
        }, window.config.branchStateRefresh);
      }
    });
  };
};

export const pollBranchStatus = (branchId) => {
  clearTimeout(pollingTimeoutId);
  return (dispatch) => {
    dispatch({
      type: ActionTypes.START_POLLING_BRANCH_STATUS,
      payload: branchId
    });
    dispatch(_pollBranchStatus(branchId));
  };
};

export const stopPollingBranchStatus = () => ({
  type: ActionTypes.STOP_POLLING_BRANCH_STATUS
});

const selectModule = (moduleId) => {
  return (dispatch) => {
    dispatch({
      type: ActionTypes.SELECT_MODULE,
      payload: moduleId
    });
    dispatch(loadModuleBuildHistory(moduleId));
  };
};

const deselectModule = () => ({
  type: ActionTypes.DESELECT_MODULE
});

export const handleModuleItemClick = (moduleId) => {
  return (dispatch, getState) => {
    const isModuleSelected = isModuleItemSelected(getState(), {moduleId});
    const action = isModuleSelected ? deselectModule() : selectModule(moduleId);
    dispatch(action);
  };
};

export const dismissBetaNotification = () => ({
  type: ActionTypes.DISMISS_BRANCH_STATE_PAGE_BETA_NOTIFICATION
});

export default {
  loadBranchStatus,
  pollBranchStatus,
  stopPollingBranchStatus,
  handleModuleItemClick,
  dismissBetaNotification
};
