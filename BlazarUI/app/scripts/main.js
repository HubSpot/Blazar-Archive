import React from 'react';
import { render } from 'react-dom';

import { createHistory } from 'history';
import { Router, useRouterHistory } from 'react-router';
import routes from './routes';

import { Provider } from 'react-redux';
import store from './reduxStore';

import { getUsernameFromCookie } from './components/Helpers.js';

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

render(
  <Provider store={store}>
    <Router history={browserHistory}>
      {routes}
    </Router>
  </Provider>,
  document.getElementById('app')
);
