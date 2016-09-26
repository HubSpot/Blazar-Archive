import Immutable from 'immutable';
import ActionTypes from '../redux-actions/ActionTypes';
import BranchInfo from '../data/records/BranchInfo';

const initialState = Immutable.Map({
  branchInfo: new BranchInfo(),
  loading: false,
  error: null
});

export default function branch(state = initialState, action) {
  switch (action.type) {
    case ActionTypes.REQUEST_BRANCH_INFO:
      return initialState.merge({
        branchInfo: new BranchInfo({id: action.payload}),
        loading: true
      });
    case ActionTypes.RECEIVE_BRANCH_INFO:
      if (action.error) {
        return initialState.set('error', Immutable.fromJS(action.payload));
      }

      return state.merge({
        branchInfo: action.payload,
        loading: false
      });
    default:
      return state;
  }
}
