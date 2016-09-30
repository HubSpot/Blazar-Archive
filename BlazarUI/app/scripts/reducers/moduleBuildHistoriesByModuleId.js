import Immutable from 'immutable';
import ActionTypes from '../redux-actions/ActionTypes';
import { PAGE_SIZE } from '../constants/ModuleBuildActivity';

const initialState = Immutable.Map({
  page: 1,
  totalPages: 0,
  moduleBuildInfos: Immutable.List(),
  loading: false,
  startingBuildNumber: null
});

function moduleBuildHistory(state = initialState, action) {
  switch (action.type) {
    case ActionTypes.SELECT_MODULE_BUILD_HISTORY_PAGE:
      return state.set('page', action.payload.page);

    case ActionTypes.REQUEST_MODULE_BUILD_HISTORY:
      return state.set('loading', true);

    case ActionTypes.RECEIVE_MODULE_BUILD_HISTORY: {
      const {page, moduleActivityPage: {moduleBuildInfos, remaining}} = action.payload;
      if (page === 1) {
        const startingBuildNumber = moduleBuildInfos.length ?
          moduleBuildInfos[0].moduleBuild.buildNumber : null;

        return state.merge({
          moduleBuildInfos,
          loading: false,
          totalPages: Math.ceil(remaining / PAGE_SIZE) + 1,
          startingBuildNumber
        });
      }

      return state.merge({
        moduleBuildInfos,
        loading: false
      });
    }

    default:
      return state;
  }
}

export default function moduleBuildHistoriesByModuleId(state = Immutable.Map(), action) {
  switch (action.type) {
    case ActionTypes.RECEIVE_MODULE_STATES:
      return Immutable.Map(action.payload.map((moduleState) => {
        const moduleId = moduleState.module.id;
        return [moduleId, state.get(moduleId, initialState)];
      }));

    case ActionTypes.SELECT_MODULE_BUILD_HISTORY_PAGE:
    case ActionTypes.REQUEST_MODULE_BUILD_HISTORY:
    case ActionTypes.RECEIVE_MODULE_BUILD_HISTORY:
      return state.update(
        action.payload.moduleId,
        (buildHistory) => moduleBuildHistory(buildHistory, action)
      );

    default:
      return state;
  }
}
