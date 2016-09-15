import ActionTypes from './ActionTypes';
import BranchStateApi from '../data/BranchStateApi';

export const loadModuleBuildHistory = (moduleId) => {
  return (dispatch) => {
    dispatch({
      type: ActionTypes.REQUEST_MODULE_BUILD_HISTORY,
      payload: {moduleId}
    });

    BranchStateApi.fetchModuleBuildHistory(moduleId).then((moduleBuilds) => {
      dispatch({
        type: ActionTypes.RECEIVE_MODULE_BUILD_HISTORY,
        payload: {moduleId, moduleBuilds}
      });
    });
  };
};

export const selectPage = (moduleId, page) => {
  return (dispatch) => {
    dispatch({
      type: ActionTypes.SELECT_MODULE_BUILD_HISTORY_PAGE,
      payload: {moduleId, page}
    });

    dispatch(loadModuleBuildHistory(moduleId));
  };
};
