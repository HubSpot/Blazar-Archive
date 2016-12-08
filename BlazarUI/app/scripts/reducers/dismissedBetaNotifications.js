import Immutable from 'immutable';
import ActionTypes from '../redux-actions/ActionTypes';

const initialState = Immutable.Map({
  branchStatePage: false
});

export default function dismissedBetaNotifications(state = initialState, action) {
  switch (action.type) {
    case ActionTypes.DISMISS_BRANCH_STATE_PAGE_BETA_NOTIFICATION:
      return state.set('branchStatePage', true);
    default:
      return state;
  }
}
