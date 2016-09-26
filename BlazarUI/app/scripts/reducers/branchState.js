import Immutable from 'immutable';
import ActionTypes from '../redux-actions/ActionTypes';

const initialState = Immutable.Map({
  moduleStates: Immutable.List(),
  selectedModuleId: null,
  loading: false,
  isPolling: false,
  branchId: null
});

export default function branchState(state = initialState, action) {
  switch (action.type) {
    case ActionTypes.START_POLLING_MODULE_STATES: {
      if (action.payload !== state.get('branchId')) {
        return initialState.merge({
          loading: true,
          isPolling: true,
          branchId: action.payload
        });
      }

      return state.set('isPolling', true);
    }
    case ActionTypes.STOP_POLLING_MODULE_STATES:
      return state.set('isPolling', false);
    case ActionTypes.RECEIVE_MODULE_STATES:
      return state.merge({
        moduleStates: action.payload,
        loading: false,
        // stop polling if api returns an empty array if the branch does not exist
        isPolling: !action.payload.length ? false : state.get('isPolling')
      });
    case ActionTypes.SELECT_MODULE:
      return state.set('selectedModuleId', action.payload);
    case ActionTypes.DESELECT_MODULE:
      return state.set('selectedModuleId', null);
    default:
      return state;
  }
}
