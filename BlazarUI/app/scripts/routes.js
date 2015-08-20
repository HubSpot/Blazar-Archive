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
import NotFound from './pages/notFound.jsx';

function pagePath(path){
  return config.appRoot + path;
}

var routes = (
  <Route name="app" path="/" handler={ App }>
    <DefaultRoute handler={ Dashboard } />
    <Route name="dashboard" path={pagePath("")} handler={ Dashboard } />
    <Route name="org" path={pagePath("builds/:url/:org/?")} handler={Org}/>
    <Route name="repo" path={pagePath("builds/:url/:org/:repo/?")} handler={Repo}/>
    <Route name="branch" path={pagePath("builds/:url/:org/:repo/:branch/?")} handler={Branch}/>
    <Route name="module" path={pagePath("builds/:url/:org/:repo/:branch/:module/?")} handler={Module}/>
    <Route name="build" path={pagePath("builds/:url/:org/:repo/:branch/:module/:buildNumber/?")} handler={Build}/>
    <NotFoundRoute handler={ NotFound } />
  </Route>
);

export default routes;
