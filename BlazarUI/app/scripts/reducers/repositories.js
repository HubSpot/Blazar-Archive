import Immutable from 'immutable';
import ActionTypes from '../redux-actions/ActionTypes';

const initialState = Immutable.Map();

export default function repositories(state = initialState, action) {
  switch (action.type) {
    case ActionTypes.RECEIVE_BRANCH_STATUS: {
      if (action.error) {
        return state;
      }

      const {branch, otherBranches} = action.payload;
      const id = branch.repositoryId;
      const branchIds = otherBranches.concat(branch).map(b => b.id);
      const repository = Immutable.Map({
        id,
        branches: Immutable.Set(branchIds)
      });
      return state.set(id, repository);
    }
    default:
      return state;
  }
}
