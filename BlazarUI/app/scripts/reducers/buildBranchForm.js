import Immutable from 'immutable';
import ActionTypes from '../redux-actions/ActionTypes';

const initialState = Immutable.Map({
  selectedModuleIds: Immutable.List(),
  buildDownstreamModules: true,
  triggerInterProjectBuild: false,
  resetCache: false,
  showModal: false,
  error: null
});

const toggleValue = (value) => !value;

export default function buildBranchForm(state = initialState, action) {
  switch (action.type) {
    case ActionTypes.SHOW_BUILD_BRANCH_MODAL:
      return initialState.set('showModal', true);
    case ActionTypes.HIDE_BUILD_BRANCH_MODAL:
      return state.set('showModal', false);

    case ActionTypes.UPDATE_BRANCH_BUILD_SELECTED_MODULE_IDS:
      return state.set('selectedModuleIds', Immutable.List(action.payload));
    case ActionTypes.TOGGLE_BUILD_DOWNSTREAM_MODULES:
      return state.set('buildDownstreamModules', action.payload);
    case ActionTypes.TOGGLE_TRIGGER_INTERPROJECT_BUILD:
      return state.update('triggerInterProjectBuild', toggleValue);
    case ActionTypes.TOGGLE_RESET_CACHE:
      return state.update('resetCache', toggleValue);

    case ActionTypes.RECEIVE_TRIGGER_BUILD_ERROR:
      return state.set('error', action.payload);

    default:
      return state;
  }
}
