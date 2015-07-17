import React from 'react';
import Router from 'react-router';
import routes from './routes';
import config from './config';

window.app = {
  config: config
};

Router.run(routes, Router.HistoryLocation, Handler => React.render(<Handler />, document.body));
