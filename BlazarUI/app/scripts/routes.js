import React from 'react';
import { Route, IndexRoute, Redirect } from 'react-router';
import $ from 'jquery';

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
import BranchState from './components/branch-state/BranchStateContainer.jsx';

import { getBranchBuildPath } from './utils/blazarPaths';

function redirectRepoBuildShortlink(nextState, replace, callback) {
  const data = $.ajax({
    url: `${window.config.apiRoot}/branches/builds/${nextState.params.repoBuildId}`,
    type: 'GET',
    dataType: 'json'
  });

  data.then((resp) => {
    replace(getBranchBuildPath(resp.branchId, resp.buildNumber));
    callback();
  }, () => {
    replace('/not-found');
    callback();
  });
}

const routes = (
  <Route name="app" path="/" component={App}>
    <IndexRoute name="dashboard" component={ Dashboard } />

    <Route name="host" path="/builds/org/:org" component={ Org } />
    <Route name="repo" path="/builds/repo/:repo" component={ Repo } />
    <Redirect from="/builds/branch/:branchId" to="/branches/:branchId/state" />

    <Route name="settings" path="/settings/branch/:branchId" component={ Settings } />
    <Route name="repoBuild" path="/builds/branch/:branchId/build/:buildNumber" component={ RepoBuild } />
    <Route name="build" path="/builds/branch/:branchId/build/:buildNumber/module/:moduleName" component={ Build } />
    <Route name="repoBuildShortlink" path="/builds/repo-build/:repoBuildId" onEnter={redirectRepoBuildShortlink} />

    <Route name="branchState" path="/branches/:branchId/state" component= { BranchState } />
    <Route name="branchBuildHistory" path="/branches/:branchId/builds" component= { Branch } />

    <Route name="notFound" path="*" component={ NotFound } />
  </Route>
);

export default routes;
