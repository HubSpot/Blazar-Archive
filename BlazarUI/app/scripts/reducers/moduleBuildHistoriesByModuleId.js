import Immutable from 'immutable';
import ActionTypes from '../redux-actions/ActionTypes';

const initialState = Immutable.Map({
  page: 1,
  totalPages: 0,
  moduleBuilds: Immutable.List(),
  loading: false
});

const PAGE_SIZE = 5;

function moduleBuildHistory(state = initialState, action) {
  switch (action.type) {
    case ActionTypes.SELECT_MODULE_BUILD_HISTORY_PAGE:
      return state.set('page', action.payload.page);

    case ActionTypes.REQUEST_MODULE_BUILD_HISTORY:
      return state.set('loading', true);

    case ActionTypes.RECEIVE_MODULE_BUILD_HISTORY: {
      const {moduleBuilds} = action.payload;
      const offset = PAGE_SIZE * (state.get('page') - 1);
      return state.merge({
        totalPages: Math.ceil(moduleBuilds.length / PAGE_SIZE),
        moduleBuilds: moduleBuilds.slice(offset, offset + PAGE_SIZE),
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
        return [moduleState.module.id, initialState];
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
