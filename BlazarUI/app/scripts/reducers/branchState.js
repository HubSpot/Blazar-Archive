import Immutable from 'immutable';

export default function branchState(state = Immutable.List(), action) {
  switch (action.type) {
    default:
      return state;
  }
}
