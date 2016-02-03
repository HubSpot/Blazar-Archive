/*global config*/
import React from 'react';
import Router from 'react-router';
import routes from './routes';

if (!config.apiRoot) {
  console.warn('You need to set your apiRoot via localStorage');
  console.warn('e.g. localStorage["apiRootOverride"] = "https://path.to-api.com/v1/api"');
}

Router.run(routes, Router.HistoryLocation, Handler => React.render(<Handler />, document.body));
