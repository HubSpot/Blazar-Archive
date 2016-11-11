import Immutable from 'immutable';
import ActionTypes from '../redux-actions/ActionTypes';
import BranchInfo from '../data/records/BranchInfo';

const initialState = Immutable.Map();

export default function branches(state = initialState, action) {
  switch (action.type) {
    case ActionTypes.RECEIVE_BRANCH_STATUS: {
      if (action.error) {
        return state;
      }

      const {branch, otherBranches} = action.payload;
      const allBranches = otherBranches.concat(branch);
      return state.merge(allBranches.map((b) => {
        return [b.id, new BranchInfo(b)];
      }));
    }
    default:
      return state;
  }
}
