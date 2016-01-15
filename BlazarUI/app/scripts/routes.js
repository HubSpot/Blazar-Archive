/*global config*/
import React from 'react';
import { Route, DefaultRoute, NotFoundRoute } from 'react-router';

// Pages
import App from './pages/app.jsx';
import Dashboard from './pages/dashboard.jsx';
import Org from './pages/org.jsx';
import Repo from './pages/repo.jsx';
import Branch from './pages/branch.jsx';
import Build from './pages/build.jsx';
import Module from './pages/module.jsx';
import RepoBuild from './pages/repoBuild.jsx';
import NotFound from './pages/notFound.jsx';

function pagePath(path) {
  return config.appRoot + path;
}

const routes = (
  <Route name='app' path='/' handler={ App }>
    <DefaultRoute handler={ Dashboard } />
    <Route name='dashboard' path={pagePath('/?')} handler={ Dashboard } />
    <Route name='org' path={pagePath('/builds/:host/:org?')} handler={Org}/>
    <Route name='repo' path={pagePath('/builds/:host/:org/:repo/?')} handler={Repo}/>
    <Route name='branch' path={pagePath('/builds/:host/:org/:repo/:branch/?')} handler={Branch}/>
    <Route name='repoBuild' path={pagePath('/builds/:host/:org/:repo/:branch/:buildNumber/?')} handler={RepoBuild}/>
    <Route name='build' path={pagePath('/builds/:host/:org/:repo/:branch/:buildNumber/:moduleName/?')} handler={Build}/>
    <NotFoundRoute handler={ NotFound } />
  </Route>
);

export default routes;
