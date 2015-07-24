import React from 'react';
import Router from 'react-router';
import routes from './routes';
import config from './config';
import ZeroClipboard from 'ZeroClipboard';

window.config = config;

// make ZeroClipboard global
// for react-zeroclipboard
window.ZeroClipboard = ZeroClipboard;

Router.run(routes, Router.HistoryLocation, Handler => React.render(<Handler />, document.body));
