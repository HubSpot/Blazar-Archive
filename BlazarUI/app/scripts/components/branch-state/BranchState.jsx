import React, { Component, PropTypes } from 'react';
import Immutable from 'immutable';
import ImmutablePropTypes from 'react-immutable-proptypes';
import { routerShape } from 'react-router';
import {bindAll} from 'underscore';

import Tabs from '../shared/Tabs.jsx';
import Tab from 'react-bootstrap/lib/Tab';

import PageContainer from '../shared/PageContainer.jsx';
import GenericErrorMessage from '../shared/GenericErrorMessage.jsx';
import Loader from '../shared/Loader.jsx';
import ModuleList from './ModuleList.jsx';
import BranchStateHeadline from './BranchStateHeadline.jsx';
import BetaFeatureAlert from './BetaFeatureAlert.jsx';
import BuildBranchModalContainer from '../shared/BuildBranchModalContainer.jsx';
import PendingBranchBuildAlert from './PendingBranchBuildAlert.jsx';
import FailingModuleBuildsAlert from './FailingModuleBuildsAlert.jsx';
import MalformedFileNotification from '../shared/MalformedFileNotification.jsx';

import ModuleBuildStates from '../../constants/ModuleBuildStates';
import { getCurrentModuleBuild, getCurrentBranchBuild, getBlazarModuleBuildPath } from '../Helpers';

class BranchState extends Component {
  constructor(props) {
    super(props);
    bindAll(this, 'handleBranchSelect', 'handleVisibilityChange', 'refreshBranchModuleStates');
  }

  componentDidMount() {
    const {pollBranchStatus, branchId} = this.props;
    pollBranchStatus(branchId);
    window.addEventListener('visibilitychange', this.handleVisibilityChange);
  }

  componentWillReceiveProps(nextProps) {
    const {pollBranchStatus, branchId} = this.props;
    if (nextProps.branchId !== branchId) {
      pollBranchStatus(nextProps.branchId);
    }
  }

  componentWillUnmount() {
    this.props.stopPollingBranchStatus();
    window.removeEventListener('visibilitychange', this.handleVisibilityChange);
  }

  sortModules(modules) {
    if (modules.isEmpty()) {
      return modules;
    }

    const currentBranchBuild = getCurrentBranchBuild(modules.first());
    const topologicalSort = currentBranchBuild && currentBranchBuild.getIn(['dependencyGraph', 'topologicalSort']);
    return modules.sort((a, b) => {
      const currentModuleBuildA = getCurrentModuleBuild(a);
      const currentModuleBuildB = getCurrentModuleBuild(b);

      // first sort by descending build number to prioritize more recent builds
      const buildNumberA = currentModuleBuildA ? currentModuleBuildA.get('buildNumber') : 0;
      const buildNumberB = currentModuleBuildB ? currentModuleBuildB.get('buildNumber') : 0;

      if (buildNumberA > buildNumberB) {
        return -1;
      } else if (buildNumberA < buildNumberB) {
        return 1;
      }

      // then sort by dependency order so that module builds proceed from top to bottom
      // gracefully handling old builds without topological sort info
      if (!topologicalSort) {
        return 1;
      }

      const indexA = topologicalSort.indexOf(a.getIn(['module', 'id']));
      const indexB = topologicalSort.indexOf(b.getIn(['module', 'id']));

      if (indexA < indexB) {
        return -1;
      }

      return 1;
    });
  }

  getFailingModuleBuildBlazarPaths(moduleStates) {
    return moduleStates
      .filter((moduleState) => {
        const currentModuleBuild = getCurrentModuleBuild(moduleState);
        return currentModuleBuild && currentModuleBuild.get('state') === ModuleBuildStates.FAILED;
      })
      .reduce((failingModuleBuildBlazarPaths, moduleState) => {
        const {branchId} = this.props;
        const moduleBuildNumber = getCurrentModuleBuild(moduleState).get('buildNumber');
        const moduleName = moduleState.getIn(['module', 'name']);
        const blazarPath = getBlazarModuleBuildPath(branchId, moduleBuildNumber, moduleName);
        return failingModuleBuildBlazarPaths.set(moduleName, blazarPath);
      }, Immutable.Map());
  }

  refreshBranchModuleStates() {
    const {branchId, loadBranchStatus} = this.props;
    loadBranchStatus(branchId);
  }

  handleBranchSelect(selectedBranchId) {
    this.props.router.push(`/branches/${selectedBranchId}/state`);
    window.document.activeElement.blur();
  }

  handleVisibilityChange() {
    if (document.hidden) {
      this.props.stopPollingBranchStatus();
    } else {
      const {pollBranchStatus, branchId} = this.props;
      pollBranchStatus(branchId);
    }
  }

  renderPendingBuilds() {
    const {pendingBranchBuilds} = this.props;

    if (pendingBranchBuilds.isEmpty()) {
      return null;
    }

    return (
      <section id="pending-branch-builds">
        {pendingBranchBuilds.map((branchBuild) =>
          <PendingBranchBuildAlert
            key={branchBuild.get('id')}
            branchBuild={branchBuild}
            onCancelBuild={this.refreshBranchModuleStates}
          />
        )}
      </section>
    );
  }

  renderModuleLists() {
    const {
      loadingBranchStatus,
      activeModules,
      inactiveModules,
      selectedModuleId,
      branchId,
      dismissBetaNotification,
      showBetaFeatureAlert,
      malformedFiles
    } = this.props;

    if (loadingBranchStatus) {
      return <Loader />;
    }

    if (!this.props.branchInfo) {
      return null;
    }

    const failingModuleBuildBlazarPaths = this.getFailingModuleBuildBlazarPaths(activeModules);
    const hasFailingModules = !!failingModuleBuildBlazarPaths.size;

    return (
      <div>
        {showBetaFeatureAlert && <BetaFeatureAlert branchId={branchId} onDismiss={dismissBetaNotification} />}
        <MalformedFileNotification malformedFiles={malformedFiles.toJS()} />
        {hasFailingModules && <FailingModuleBuildsAlert failingModuleBuildBlazarPaths={failingModuleBuildBlazarPaths} />}
        {this.renderPendingBuilds()}
        <Tabs id="branch-state-tabs" className="branch-state-tabs" defaultActiveKey="active-modules">
          <Tab eventKey="active-modules" title="Active modules">
            <section id="active-modules">
              <ModuleList
                modules={this.sortModules(activeModules)}
                onCancelBuild={this.refreshBranchModuleStates}
              />
            </section>
          </Tab>

          {!!inactiveModules.size && (
            <Tab title="Inactive modules" eventKey="inactive-modules">
              <section id="inactive-modules">
                <p className="text-muted">Showing previous builds of modules no longer contained in this branch.</p>
                <ModuleList
                  modules={this.sortModules(inactiveModules)}
                  onCancelBuild={this.refreshBranchModuleStates}
                />
              </section>
            </Tab>
          )}
        </Tabs>
      </div>
    );
  }

  render() {
    const {
      activeModules,
      branchId,
      branchInfo,
      branchNotFound,
      errorMessage
    } = this.props;

    const title = branchInfo ? `${branchInfo.repository}-${branchInfo.branch}` : 'Branch State';

    if (branchNotFound) {
      return (
        <PageContainer classNames="page-content--branch-state" documentTitle={title}>
          <GenericErrorMessage message="Branch not found." />
        </PageContainer>
      );
    }

    return (
      <PageContainer classNames="page-content--branch-state" documentTitle={title}>
        <BranchStateHeadline branchId={branchId} branchInfo={branchInfo} onBranchSelect={this.handleBranchSelect} />
        <GenericErrorMessage message={errorMessage} />
        {this.renderModuleLists()}
        <BuildBranchModalContainer
          branchId={branchId}
          modules={activeModules.map(moduleState => moduleState.get('module')).toJS()}
          onBuildStart={this.refreshBranchModuleStates}
        />
      </PageContainer>
    );
  }
}

BranchState.propTypes = {
  branchId: PropTypes.number.isRequired,
  branchInfo: PropTypes.object,
  activeModules: ImmutablePropTypes.list,
  inactiveModules: ImmutablePropTypes.list,
  pendingBranchBuilds: ImmutablePropTypes.list,
  malformedFiles: ImmutablePropTypes.list,
  router: routerShape,
  selectedModuleId: PropTypes.number,
  loadBranchStatus: PropTypes.func.isRequired,
  pollBranchStatus: PropTypes.func.isRequired,
  stopPollingBranchStatus: PropTypes.func.isRequired,
  loadingBranchStatus: PropTypes.bool.isRequired,
  branchNotFound: PropTypes.bool,
  showBetaFeatureAlert: PropTypes.bool,
  dismissBetaNotification: PropTypes.func.isRequired,
  errorMessage: PropTypes.string
};

export default BranchState;
