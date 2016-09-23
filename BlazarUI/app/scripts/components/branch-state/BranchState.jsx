import React, { Component, PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import { routerShape } from 'react-router';
import {bindAll} from 'underscore';

import PageContainer from '../shared/PageContainer.jsx';
import Loader from '../shared/Loader.jsx';
import ModuleList from './ModuleList.jsx';
import BranchStateHeadline from './BranchStateHeadline.jsx';
import BuildBranchModalContainer from '../shared/BuildBranchModalContainer.jsx';

class BranchState extends Component {
  constructor(props) {
    super(props);
    bindAll(this, 'handleBranchSelect', 'handleModuleItemClick');
  }

  componentDidMount() {
    const {pollBranchModuleStates, branchId} = this.props;
    pollBranchModuleStates(branchId);
  }

  componentWillReceiveProps(nextProps) {
    const {pollBranchModuleStates, branchId} = this.props;
    if (nextProps.branchId !== branchId) {
      pollBranchModuleStates(nextProps.branchId);
    }
  }

  componentWillUnmount() {
    this.props.stopPollingBranchModuleStates();
  }

  sortActiveModules() {
    const {activeModules} = this.props;
    if (activeModules.isEmpty()) {
      return activeModules;
    }

    const topologicalSort = activeModules.first().getIn(['lastRepoBuild', 'dependencyGraph', 'topologicalSort']);
    return activeModules.sort((a, b) => {
      const indexA = topologicalSort.indexOf(a.getIn(['module', 'id']));
      const indexB = topologicalSort.indexOf(b.getIn(['module', 'id']));

      if (indexA < indexB) {
        return -1;
      }

      return 1;
    });
  }

  handleBranchSelect(selectedBranchId) {
    this.props.router.push(`/branchState/branch/${selectedBranchId}`);
  }

  handleModuleItemClick(id) {
    const {selectModule, deselectModule, selectedModuleId} = this.props;
    if (id === selectedModuleId) {
      deselectModule();
    } else {
      selectModule(id);
    }
  }

  renderModuleLists() {
    const {loadingModuleStates, activeModules, inactiveModules, selectedModuleId} = this.props;
    if (loadingModuleStates) {
      return <Loader />;
    }

    return (
      <div>
        <section id="active-modules">
          <h2 className="module-list-header">Active modules</h2>
          <ModuleList modules={this.sortActiveModules(activeModules)} onItemClick={this.handleModuleItemClick} selectedModuleId={selectedModuleId} />
        </section>
        {!!inactiveModules.size && <section id="inactive-modules">
          <h2 className="module-list-header">Inactive modules</h2>
          <ModuleList modules={inactiveModules} onItemClick={this.handleModuleItemClick} selectedModuleId={selectedModuleId} />
        </section>}
      </div>
    );
  }

  render() {
    const {
      activeModules,
      branchId,
      branchInfo,
      loadBranchModuleStates
    } = this.props;

    const title = branchInfo.branch ? `${branchInfo.repository}-${branchInfo.branch}` : 'Branch State';

    return (
      <PageContainer classNames="page-content--branch-state" documentTitle={title}>
        <BranchStateHeadline branchId={branchId} branchInfo={branchInfo} onBranchSelect={this.handleBranchSelect} />
        {this.renderModuleLists()}
        <BuildBranchModalContainer
          branchId={branchId}
          modules={activeModules.map(moduleState => moduleState.get('module')).toJS()}
          onBuildStart={() => loadBranchModuleStates(branchId)}
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
  router: routerShape,
  selectModule: PropTypes.func.isRequired,
  deselectModule: PropTypes.func.isRequired,
  selectedModuleId: PropTypes.number,
  loadBranchModuleStates: PropTypes.func.isRequired,
  pollBranchModuleStates: PropTypes.func.isRequired,
  stopPollingBranchModuleStates: PropTypes.func.isRequired,
  loadingModuleStates: PropTypes.bool.isRequired
};

export default BranchState;
