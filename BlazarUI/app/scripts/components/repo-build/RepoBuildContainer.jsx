import React, {Component, PropTypes} from 'react';
import {bindAll, clone, some} from 'underscore';

import PageContainer from '../shared/PageContainer.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import GenericErrorMessage from '../shared/GenericErrorMessage.jsx';
import Loader from '../shared/Loader.jsx';

import StarStore from '../../stores/starStore';
import StarActions from '../../actions/starActions';

import RepoBuildStore from '../../stores/repoBuildStore';
import RepoBuildActions from '../../actions/repoBuildActions';

import BranchStore from '../../stores/branchStore';
import BranchActions from '../../actions/branchActions';

import InterProjectStore from '../../stores/interProjectStore';
import InterProjectActions from '../../actions/interProjectActions';

import RepoBuildHeadline from './RepoBuildHeadline.jsx';
import RepoBuildModulesTable from './RepoBuildModulesTable.jsx';
import RepoBuildDetail from './RepoBuildDetail.jsx';

import MalformedFileNotification from '../shared/MalformedFileNotification.jsx';

let initialState = {
  moduleBuilds: false,
  stars: [],
  malformedFiles: [],
  loadingMalformedFiles: true,
  loadingModuleBuilds: true,
  loadingRepoBuild: true,
  loadingStars: true,
  branchInfo: {},
  upAndDownstreamModules: {},
  currentRepoBuild: null,
  loadingUpAndDownstreamModules: true
};

class RepoBuildContainer extends Component {

  constructor(props) {
    super(props);
    this.state = initialState;

    bindAll(this, 'onStatusChange', 'triggerCancelBuild', 'tryLoadInterProjectBuildMapping');
  }

  componentDidMount() {
    this.setup(this.props.params);
  }

  componentWillReceiveProps(nextprops) {
    this.tearDown();
    this.setState(initialState, () => {
      this.setup(nextprops.params);
    });
  }

  componentWillUnmount() {
    this.tearDown();
  }

  setup(params) {
    this.unsubscribeFromStars = StarStore.listen(this.onStatusChange);
    this.unsubscribeFromRepoBuild = RepoBuildStore.listen(this.onStatusChange);
    this.unsubscribeFromBranch = BranchStore.listen(this.onStatusChange);
    this.unsubscribeFromInterProject = InterProjectStore.listen(this.onStatusChange);
    StarActions.loadStars('repoBuildContainer');
    RepoBuildActions.loadModuleBuilds(params);
    RepoBuildActions.loadRepoBuild(params);
    RepoBuildActions.startPolling(params);
    BranchActions.loadBranchInfo(params);
    BranchActions.loadMalformedFiles(params);
    this.tryLoadInterProjectBuildMapping();
  }

  tryLoadInterProjectBuildMapping() {
    const {currentRepoBuild} = this.state;

    if (!currentRepoBuild) {
      setTimeout(this.tryLoadInterProjectBuildMapping, 100);
      return;
    }

    InterProjectActions.getUpAndDownstreamModules(currentRepoBuild.id);
  }

  tearDown() {
    RepoBuildActions.stopPolling();
    this.unsubscribeFromStars();
    this.unsubscribeFromRepoBuild();
    this.unsubscribeFromBranch();
    this.unsubscribeFromInterProject();
  }

  onStatusChange(state) {
    this.setState(state);
  }

  triggerCancelBuild() {
    RepoBuildActions.cancelBuild(this.props.params);
  }

  isLoading() {
    const {loadingStars, loadingModuleBuilds, loadingRepoBuild, moduleBuilds, loadingUpAndDownstreamModules} = this.state;

    return loadingStars
      || loadingModuleBuilds
      || loadingRepoBuild
      || !moduleBuilds
      || loadingUpAndDownstreamModules;
  }

  buildDocumentTitle() {
    const {branchInfo, currentRepoBuild} = this.state;
    const titlePrefix = currentRepoBuild ? `#${currentRepoBuild.buildNumber}` : `#${this.props.params.buildNumber}`;
    const titleInfo = branchInfo ? ' | ' + branchInfo.repository  + ' - ' + branchInfo.branch : '';

    return titlePrefix + titleInfo;
  }

  renderSectionContent() {
    if (this.state.error) {
      return this.renderError();
    }

    return this.renderPage();
  }

  renderError() {
    return (
      <UIGrid>
        <UIGridItem size={10}>
          <GenericErrorMessage
            message={this.state.error}
          />
        </UIGridItem>
      </UIGrid>
    );
  }

  renderMalformedFileAlert() {
    if (this.state.loadingMalformedFiles || this.state.malformedFiles.length === 0) {
      return null;
    }

    return (
      <UIGrid>
        <UIGridItem size={12}>
          <MalformedFileNotification
          loading={this.state.loadingMalformedFiles}
          malformedFiles={this.state.malformedFiles} />
        </UIGridItem>
      </UIGrid>
    );
  }

  renderPage() {
    return (
      <div>
        {this.renderMalformedFileAlert()}
        <UIGrid>
          <UIGridItem size={11}>
            <RepoBuildHeadline
              {...this.props}
              {...this.state}
              branchInfo={this.state.branchInfo}
              loading={this.isLoading()}
            />
          </UIGridItem>
        </UIGrid>
        <UIGridItem size={12}>
          <RepoBuildDetail
            {...this.props}
            {...this.state}
            loading={this.isLoading()}
            triggerCancelBuild={this.triggerCancelBuild}
          />
          <RepoBuildModulesTable
            params={this.props.params}
            data={this.state.moduleBuilds || []}
            currentRepoBuild={this.state.currentRepoBuild}
            loading={this.isLoading()}
          />
        </UIGridItem>
      </div>
    );
  }

  render() {
    const documentTitle = this.buildDocumentTitle();

    return (
      <PageContainer documentTitle={documentTitle}>
        {this.renderSectionContent()}
      </PageContainer>
    );
  }
}

RepoBuildContainer.propTypes = {
  params: PropTypes.object.isRequired
};

export default RepoBuildContainer;
