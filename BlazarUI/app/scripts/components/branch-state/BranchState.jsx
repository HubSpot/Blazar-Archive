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

import ModuleBuildStates from '../../constants/ModuleBuildStates';
import { getCurrentModuleBuild, getCurrentBranchBuild, getBlazarModuleBuildPath } from '../Helpers';

class BranchState extends Component {
  constructor(props) {
    super(props);
    bindAll(this, 'handleBranchSelect', 'handleModuleItemClick', 'handleVisibilityChange', 'refreshBranchModuleStates');
  }

  componentDidMount() {
    const {pollBranchModuleStates, branchId, loadBranchInfo} = this.props;
    pollBranchModuleStates(branchId);
    loadBranchInfo(branchId);
    window.addEventListener('visibilitychange', this.handleVisibilityChange);
  }

  componentWillReceiveProps(nextProps) {
    const {pollBranchModuleStates, branchId, loadBranchInfo} = this.props;
    if (nextProps.branchId !== branchId) {
      pollBranchModuleStates(nextProps.branchId);
      loadBranchInfo(nextProps.branchId);
    }
  }

  componentWillUnmount() {
    this.props.stopPollingBranchModuleStates();
    window.removeEventListener('visibilitychange', this.handleVisibilityChange);
  }

  sortModules(modules) {
    if (modules.isEmpty()) {
      return modules;
    }

    const topologicalSort = getCurrentBranchBuild(modules.first()).getIn(['dependencyGraph', 'topologicalSort']);
    return modules.sort((a, b) => {
      // first sort by descending build number to prioritize more recent builds
      const buildNumberA = getCurrentModuleBuild(a).get('buildNumber');
      const buildNumberB = getCurrentModuleBuild(b).get('buildNumber');

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
        return getCurrentModuleBuild(moduleState).get('state') === ModuleBuildStates.FAILED;
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
    const {branchId, loadBranchModuleStates} = this.props;
    loadBranchModuleStates(branchId);
  }

  handleBranchSelect(selectedBranchId) {
    this.props.router.push(`/branches/${selectedBranchId}/state`);
  }

  handleModuleItemClick(id) {
    const {selectModule, deselectModule, selectedModuleId} = this.props;
    if (id === selectedModuleId) {
      deselectModule();
    } else {
      selectModule(id);
    }
  }

  handleVisibilityChange() {
    if (document.hidden) {
      this.props.stopPollingBranchModuleStates();
    } else {
      const {pollBranchModuleStates, branchId} = this.props;
      pollBranchModuleStates(branchId);
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
            branchBuild={branchBuild}
            onCancelBuild={this.refreshBranchModuleStates}
          />
        )}
      </section>
    );
  }

  renderModuleLists() {
    const {
      loadingModuleStates,
      activeModules,
      inactiveModules,
      selectedModuleId,
      branchId,
      dismissBetaNotification,
      showBetaFeatureAlert
    } = this.props;

    const loadingHeader = !this.props.branchInfo.branch;
    if (loadingModuleStates || loadingHeader) {
      return <Loader />;
    }

    const failingModuleBuildBlazarPaths = this.getFailingModuleBuildBlazarPaths(activeModules);
    const hasFailingModules = !!failingModuleBuildBlazarPaths.size;

    return (
      <div>
        {showBetaFeatureAlert && <BetaFeatureAlert branchId={branchId} onDismiss={dismissBetaNotification} />}
        {hasFailingModules && <FailingModuleBuildsAlert failingModuleBuildBlazarPaths={failingModuleBuildBlazarPaths} />}
        {this.renderPendingBuilds()}
        <Tabs id="branch-state-tabs" className="branch-state-tabs" defaultActiveKey="active-modules">
          <Tab eventKey="active-modules" title="Active modules">
            <section id="active-modules">
              <ModuleList
                modules={this.sortModules(activeModules)}
                onItemClick={this.handleModuleItemClick}
                selectedModuleId={selectedModuleId}
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
                  onItemClick={this.handleModuleItemClick}
                  selectedModuleId={selectedModuleId}
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
      branchNotFound
    } = this.props;

    const title = branchInfo.branch ? `${branchInfo.repository}-${branchInfo.branch}` : 'Branch State';

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
  router: routerShape,
  selectModule: PropTypes.func.isRequired,
  deselectModule: PropTypes.func.isRequired,
  selectedModuleId: PropTypes.number,
  loadBranchInfo: PropTypes.func.isRequired,
  loadBranchModuleStates: PropTypes.func.isRequired,
  pollBranchModuleStates: PropTypes.func.isRequired,
  stopPollingBranchModuleStates: PropTypes.func.isRequired,
  loadingModuleStates: PropTypes.bool.isRequired,
  branchNotFound: PropTypes.bool,
  showBetaFeatureAlert: PropTypes.bool,
  dismissBetaNotification: PropTypes.func.isRequired
};

BranchState.defaultProps = {
  pendingBranchBuilds: Immutable.fromJS([{
    branchId: 2,
    buildNumber: 143,
    id: 172,
    buildTrigger: {
      id: 'jgoodwin',
      type: 'MANUAL'
    },
    state: 'PENDING'
  }, {
    branchId: 2,
    buildNumber: 142,
    id: 72,
    buildTrigger: {
      type: 'PUSH',
      id: 'f2ffad8f0e44311051a6b8b2e1d814fbfa638a06'
    },
    state: 'PENDING'
  }, {
    branchId: 2,
    buildNumber: 141,
    id: 174,
    buildTrigger: {
      type: 'INTER_PROJECT',
      id: '80'
    },
    state: 'LAUNCHING'
  }])
};

export default BranchState;
