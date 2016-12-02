export const getModuleBuildHistory = (state, {moduleId}) =>
  state.moduleBuildHistoriesByModuleId.get(moduleId);

export const isModuleItemSelected = (state, {moduleId}) =>
  moduleId === state.branchState.get('selectedModuleId');

export const shouldExpandModuleBuildHistory = (state, {moduleId}) => {
  if (!isModuleItemSelected(state, {moduleId})) {
    return false;
  }

  const buildHistory = getModuleBuildHistory(state, {moduleId});

  const lastRequestStartTime = buildHistory.get('lastRequestStartTime');
  const neverAttemptedToFetchData = !lastRequestStartTime;
  if (neverAttemptedToFetchData) {
    return false;
  }

  const hasPreviouslyLoadedData = buildHistory.get('totalPages') > 0;
  if (hasPreviouslyLoadedData) {
    return true;
  }

  // initial fetch was started but data has not been loaded yet
  // adding a delay smooths out the expansion of the module item details
  const initialFetchTakingToLong = Date.now() - lastRequestStartTime > 500;
  return initialFetchTakingToLong;
};
