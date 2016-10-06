import { createStore, applyMiddleware, compose } from 'redux';
import thunk from 'redux-thunk';
import { throttle } from 'underscore';

import rootReducer from './reducers';
import ActionTypes from './redux-actions/ActionTypes';
import {
  loadStarredBranches,
  saveStarredBranches,
  onStarredBranchesUpdate,
  loadDismissedBetaNotifications,
  saveDismissedBetaNotifications
} from './localStorage';

const store = createStore(
  rootReducer,
  {
    starredBranches: loadStarredBranches(),
    dismissedBetaNotifications: loadDismissedBetaNotifications()
  },
  compose(
    applyMiddleware(thunk),
    window.devToolsExtension ? window.devToolsExtension() : f => f
  )
);

store.subscribe(throttle(() => {
  const { starredBranches, dismissedBetaNotifications } = store.getState();
  saveStarredBranches(starredBranches);
  saveDismissedBetaNotifications(dismissedBetaNotifications);
}, 1000));

onStarredBranchesUpdate((starredBranches) => {
  store.dispatch({
    type: ActionTypes.SYNC_STARRED_BRANCHES,
    payload: starredBranches
  });
});

export default store;
