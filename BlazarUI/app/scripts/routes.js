/*global config*/
import React from 'react';
import { Route, IndexRoute } from 'react-router';

// Pages
import App from './pages/app.jsx';
import Dashboard from './pages/dashboard.jsx';
import Org from './pages/org.jsx';
import Repo from './pages/repo.jsx';
import Branch from './pages/branch.jsx';
import Build from './pages/build.jsx';
import RepoBuild from './pages/repoBuild.jsx';
import NotFound from './pages/notFound.jsx';

const routes = (
  <Route name='app' path='/' component={App}>
    <IndexRoute name='dashboard' component={ Dashboard } />
    <Route name='org' path='/builds/:host/:org' component={Org} />
    <Route name='repo' path='/builds/:host/:org/:repo' component={Repo} />
    <Route name='branch' path='/builds/:host/:org/:repo/:branch' component={Branch} />
    <Route name='repoBuild' path='/builds/:host/:org/:repo/:branch/:buildNumber' component={RepoBuild} />
    <Route name='build' path='/builds/:host/:org/:repo/:branch/:buildNumber/:moduleName' component={Build} />
    <Route name='notFound' path="*" component={ NotFound } />
  </Route>
);

export default routes;
