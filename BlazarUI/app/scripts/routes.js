/*global config*/
import React from 'react';
import { Route, IndexRoute } from 'react-router';

// Pages
import App from './pages/app.jsx';
import Dashboard from './pages/dashboard.jsx';
import Org from './pages/org.jsx';
import Repo from './pages/repo.jsx';
import Branch from './pages/branch.jsx';
import Settings from './pages/settings.jsx';
import Build from './pages/build.jsx';
import RepoBuild from './pages/repoBuild.jsx';
import NotFound from './pages/notFound.jsx';

const routes = (
  <Route name='app' path='/' component={App}>
    <IndexRoute name='dashboard' component={ Dashboard } />
    <Route name='branch' path='/builds/branch/:branchId' component={Branch} />
    <Route name='settings' path='/settings/branch/:branchId' component={Settings} />
    <Route name='repoBuild' path='/builds/branch/:branchId/build/:buildNumber' component={RepoBuild} />
    <Route name='build' path='/builds/:host/:org/:repo/:branch/:buildNumber/:moduleName' component={Build} />
    <Route name='notFound' path="*" component={ NotFound } />
  </Route>
);

export default routes;
