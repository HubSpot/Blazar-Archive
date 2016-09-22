import ActionTypes from './ActionTypes';
import InterProjectApi from '../data/InterProjectApi';
import BranchApi from '../data/BranchApi';

export const showBranchBuildModal = () => ({
  type: ActionTypes.SHOW_BRANCH_BUILD_MODAL
});

export const hideBranchBuildModal = () => ({
  type: ActionTypes.HIDE_BRANCH_BUILD_MODAL
});

export const updateBranchBuildSelectedModuleIds = (ids) => ({
  type: ActionTypes.UPDATE_BRANCH_BUILD_SELECTED_MODULE_IDS,
  payload: ids
});

export const toggleBuildDownstreamModules = () => ({
  type: ActionTypes.TOGGLE_BUILD_DOWNSTREAM_MODULES
});

export const toggleTriggerInterProjectBuild = () => ({
  type: ActionTypes.TOGGLE_TRIGGER_INTERPROJECT_BUILD
});

export const toggleResetCache = () => ({
  type: ActionTypes.TOGGLE_RESET_CACHE
});

export const triggerBuild = (branchId, {selectedModuleIds, resetCache, buildDownstreamModules, triggerInterProjectBuild}, onBuildStart) => {
  return (dispatch) => {
    const buildPromise = triggerInterProjectBuild ?
      InterProjectApi.triggerInterProjectBuild(selectedModuleIds, resetCache) :
      BranchApi.triggerBuild(branchId, selectedModuleIds, buildDownstreamModules, resetCache);

    buildPromise.then(() => {
      if (onBuildStart) {
        onBuildStart();
      }
    }, (error) => {
      console.warn(error);
      dispatch({
        type: ActionTypes.RECEIVE_TRIGGER_BUILD_ERROR,
        payload: 'Error triggering build. Check your console for more detail.'
      });
    });
  };
};
