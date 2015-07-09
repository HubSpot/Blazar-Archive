import React from 'react';
import { Route, DefaultRoute, NotFoundRoute } from 'react-router';

import App from './pages/app.jsx';
import Dashboard from './pages/dashboard.jsx';
import Project from './pages/project.jsx';
import NotFound from './pages/notFound.jsx';

var routes = (
  <Route name="app" path="/" handler={ App }>
    <Route name="dashboard" handler={ Dashboard } />
    <DefaultRoute handler={ Dashboard } />
    <Route name="project" path="/project/:id" handler={Project}/>
    <NotFoundRoute handler={ NotFound } />
  </Route>
);

export default routes;