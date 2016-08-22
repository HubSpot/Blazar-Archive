import React from 'react';
import { render } from 'react-dom';
import { createHistory } from 'history';
import { Router, useRouterHistory } from 'react-router';
import routes from './routes';
import { getUsernameFromCookie } from './components/Helpers.js';

import { createStore, applyMiddleware, compose } from 'redux';
import thunk from 'redux-thunk';
import { Provider } from 'react-redux';
import rootReducer from './reducers';

if (!window.config.apiRoot) {
  console.warn('You need to set your apiRoot via localStorage');
  console.warn('e.g. localStorage["apiRootOverride"] = "https://path.to-api.com/v1/api"');
}

if (window.config.heapToken) {
  const username = getUsernameFromCookie();
  window.heap.identify(`${username}-blazar`);
  window.heap.addUserProperties({
    'Name': username,
    'App': 'blazar'
  });
}

const browserHistory = useRouterHistory(createHistory)({
  basename: window.config.appRoot
});

const store = compose(
  applyMiddleware(thunk),
  window.devToolsExtension ? window.devToolsExtension() : f => f
)(createStore)(rootReducer);

render(
  <Provider store={store}>
    <Router history={browserHistory}>
      {routes}
    </Router>
  </Provider>,
  document.getElementById('app')
);
