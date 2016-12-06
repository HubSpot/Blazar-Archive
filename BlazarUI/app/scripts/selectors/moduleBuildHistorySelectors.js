import { createSelector } from 'reselect';

export const getModuleBuildHistory = (state, {moduleId}) =>
  state.moduleBuildHistoriesByModuleId.get(moduleId);

export const isModuleItemSelected = (state, {moduleId}) =>
  moduleId === state.branchState.get('selectedModuleId');

export const getLastRequestStartTime = createSelector([getModuleBuildHistory],
  (buildHistory) => buildHistory.get('lastRequestStartTime')
);

export const hasPreviouslyLoadedData = createSelector([getModuleBuildHistory],
  (buildHistory) => buildHistory.get('totalPages') > 0
);
