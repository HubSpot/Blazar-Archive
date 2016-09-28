import ActionTypes from './ActionTypes';
import BranchStateApi from '../data/BranchStateApi';
import { PAGE_SIZE } from '../constants/ModuleBuildActivity';

const computeBuildNumberOffset = (buildHistory, page) => {
  if (page === 1) return null;
  const startingBuildNumber = buildHistory.get('startingBuildNumber');
  return startingBuildNumber - (page - 1) * PAGE_SIZE;
};

export const loadModuleBuildHistory = (moduleId, maybePage) => {
  return (dispatch, getState) => {
    dispatch({
      type: ActionTypes.REQUEST_MODULE_BUILD_HISTORY,
      payload: {moduleId}
    });

    const buildHistory = getState().moduleBuildHistoriesByModuleId.get(moduleId);
    const page = maybePage || buildHistory.get('page');
    const offset = computeBuildNumberOffset(buildHistory, page);
    BranchStateApi.fetchModuleBuildHistory(moduleId, offset, PAGE_SIZE).then((moduleActivityPage) => {
      dispatch({
        type: ActionTypes.RECEIVE_MODULE_BUILD_HISTORY,
        payload: {moduleId, moduleActivityPage, page}
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

    dispatch(loadModuleBuildHistory(moduleId, page));
  };
};
