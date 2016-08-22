import Immutable, {fromJS} from 'immutable';
import mockBranchState from '../data/mockBranchState';

const initialState = Immutable.Map({
  polledState: fromJS(mockBranchState)
});

export default function branchState(state = initialState, action) {
  switch (action.type) {
    default:
      return state;
  }
}
