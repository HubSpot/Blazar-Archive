/*global config*/
import React from 'react';
import { render } from 'react-dom';
import { createHistory } from 'history';
import { Router, useRouterHistory } from 'react-router';
import routes from './routes';
import Cookies from 'js-cookie';

if (!config.apiRoot) {
  console.warn('You need to set your apiRoot via localStorage');
  console.warn('e.g. localStorage["apiRootOverride"] = "https://path.to-api.com/v1/api"');
}

if (config.fullstoryToken && Cookies.get(config.usernameCookie)) {
  console.log("Got in the if");
  FS.identify(Cookies.get(config.usernameCookie), {
    app: 'blazar'
  });
} 

else {
  console.log("Did not get in the if");
}

console.log("Fullstory token: ", config.fullstoryToken);
console.log("Cookie key: ", config.usernameCookie);
console.log("Cookie val: ", Cookies.get(config.usernameCookie));

const browserHistory = useRouterHistory(createHistory) ({
  basename: config.appRoot
});

render(
  <Router history={browserHistory}>
    {routes}
  </Router>,
  document.getElementById('app')
);
