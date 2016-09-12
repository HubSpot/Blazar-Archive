import Immutable from 'immutable';
import ActionTypes from '../redux-actions/ActionTypes';

const initialState = Immutable.Map({
  page: 1,
  totalPages: 20
});

function moduleBuildHistory(state = initialState, action) {
  switch (action.type) {
    case ActionTypes.SELECT_MODULE_BUILD_HISTORY_PAGE:
      return state.set('page', action.payload.page);
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
      return state.map((buildHistory, moduleId) => {
        return (moduleId === action.payload.moduleId) ?
          moduleBuildHistory(buildHistory, action) : buildHistory;
      });
    default:
      return state;
  }
}
