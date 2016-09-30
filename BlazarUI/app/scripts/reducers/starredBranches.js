import Immutable from 'immutable';
import ActionTypes from '../redux-actions/ActionTypes';

export default function repo(state = Immutable.Set(), action) {
  switch (action.type) {
    case ActionTypes.TOGGLE_STARRED_BRANCH: {
      const parsedBranchId = parseInt(action.payload, 10);
      return state.has(parsedBranchId) ?
        state.delete(parsedBranchId) : state.add(parsedBranchId);
    }

    case ActionTypes.SYNC_STARRED_BRANCHES:
      return action.payload;

    default:
      return state;
  }
}
