import { createStore, applyMiddleware, compose } from 'redux';
import thunk from 'redux-thunk';
import { throttle } from 'underscore';

import rootReducer from './reducers';
import { loadStarredBranches, saveStarredBranches, onStarredBranchesUpdate } from './localStorage';
import ActionTypes from './redux-actions/ActionTypes';

const store = createStore(
  rootReducer,
  {starredBranches: loadStarredBranches()},
  compose(
    applyMiddleware(thunk),
    window.devToolsExtension ? window.devToolsExtension() : f => f
  )
);

store.subscribe(throttle(() => {
  saveStarredBranches(store.getState().starredBranches);
}, 1000));

onStarredBranchesUpdate((starredBranches) => {
  store.dispatch({
    type: ActionTypes.SYNC_STARRED_BRANCHES,
    payload: starredBranches
  });
});

export default store;
