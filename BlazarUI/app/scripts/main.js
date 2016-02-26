/*global config*/
import React from 'react';
import { render } from 'react-dom';
import {Router, browserHistory} from 'react-router';
import routes from './routes';

if (!config.apiRoot) {
  console.warn('You need to set your apiRoot via localStorage');
  console.warn('e.g. localStorage["apiRootOverride"] = "https://path.to-api.com/v1/api"');
}

render(
	<Router history={browserHistory}>
		{routes}
	</Router>, 
	document.getElementById('new-app-container')
);
