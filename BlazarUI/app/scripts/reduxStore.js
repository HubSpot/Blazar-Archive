import { createStore, applyMiddleware, compose } from 'redux';
import thunk from 'redux-thunk';

import rootReducer from './reducers';

import { loadStarredBranches, saveStarredBranches } from './localStorage';
import { throttle } from 'underscore';

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

export default store;
