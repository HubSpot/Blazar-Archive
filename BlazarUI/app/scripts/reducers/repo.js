import Immutable from 'immutable';
import ActionTypes from '../redux-actions/ActionTypes';

const initialState = Immutable.Map({
  repoId: null,
  branchesList: Immutable.List(),
  loading: false
});

export default function repo(state = initialState, action) {
  switch (action.type) {
    case ActionTypes.REQUEST_BRANCHES_IN_REPO:
      return initialState.merge({
        repoId: action.payload,
        loading: true
      });
    case ActionTypes.RECEIVE_BRANCHES_IN_REPO:
      return state.merge({
        branchesList: action.payload,
        loading: false
      });
    default:
      return state;
  }
}
