import ActionTypes from './ActionTypes';

export const selectPage = (moduleId, page) => ({
  type: ActionTypes.SELECT_MODULE_BUILD_HISTORY_PAGE,
  payload: {
    moduleId,
    page
  }
});
