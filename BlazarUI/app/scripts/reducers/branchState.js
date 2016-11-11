import Immutable from 'immutable';
import ActionTypes from '../redux-actions/ActionTypes';

const initialState = Immutable.Map({
  moduleStates: Immutable.List(),
  selectedModuleId: null,
  loading: false,
  isPolling: false,
  branchId: null,
  branchNotFound: false,
  error: null,
  malformedFiles: Immutable.List(),
  queuedBuilds: Immutable.List()
});

export default function branchState(state = initialState, action) {
  switch (action.type) {
    case ActionTypes.START_POLLING_BRANCH_STATUS: {
      if (action.payload !== state.get('branchId')) {
        return initialState.merge({
          loading: true,
          isPolling: true,
          branchId: action.payload
        });
      }

      return state.set('isPolling', true);
    }
    case ActionTypes.STOP_POLLING_BRANCH_STATUS:
      return state.set('isPolling', false);
    case ActionTypes.RECEIVE_BRANCH_STATUS: {
      if (action.error) {
        return initialState.merge({
          branchNotFound: action.payload.status === 404,
          error: action.payload.responseJSON,
          branchId: state.get('branchId')
        });
      }

      const {moduleStates, malformedFiles, queuedBuilds} = action.payload;
      return state.merge({
        moduleStates,
        loading: false,
        // stop polling if api returns an empty array if the branch does not exist
        isPolling: moduleStates && moduleStates.length && state.get('isPolling'),
        malformedFiles,
        queuedBuilds
      });
    }
    case ActionTypes.SELECT_MODULE:
      return state.set('selectedModuleId', action.payload);
    case ActionTypes.DESELECT_MODULE:
      return state.set('selectedModuleId', null);
    default:
      return state;
  }
}
