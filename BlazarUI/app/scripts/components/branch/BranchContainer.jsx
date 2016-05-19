import React, {Component, PropTypes} from 'react';
import {Link} from 'react-router';
import {Button} from 'react-bootstrap';
import {bindAll} from 'underscore';
import classNames from 'classnames';
import PageContainer from '../shared/PageContainer.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import Headline from '../shared/headline/Headline.jsx';
import HeadlineDetail from '../shared/headline/HeadlineDetail.jsx';
import BranchBuildHistoryTable from './BranchBuildHistoryTable.jsx';
import BranchHeadline from './BranchHeadline.jsx';
import Loader from '../shared/Loader.jsx';
import GenericErrorMessage from '../shared/GenericErrorMessage.jsx';
import BuildButton from './BuildButton.jsx';
import ModuleModal from '../shared/ModuleModal.jsx';
import MalformedFileNotification from '../shared/MalformedFileNotification.jsx';
import Immutable from 'immutable';
import $ from 'jquery';

import StarStore from '../../stores/starStore';
import StarActions from '../../actions/starActions';

import BranchStore from '../../stores/branchStore';
import BranchActions from '../../actions/branchActions';
import InterProjectStore from '../../stores/interProjectStore';
import InterProjectActions from '../../actions/interProjectActions';
import RepoStore from '../../stores/repoStore';
import RepoActions from '../../actions/repoActions';

import {getPreviousBuildState} from '../Helpers.js';

let initialState = {
  builds: null,
  stars: [],
  loadingBranches: true,
  loadingStars: true,
  loadingModules: true,
  loadingMalformedFiles: true,
  loadingRepo: true,
  malformedFiles: [],
  showModuleModal: false,
  modules: [],
  selectedModules: [],
  buildDownstreamModules: 'WITHIN_REPOSITORY',
  triggerInterProjectBuild: false,
  resetCache: false,
  branchId: 0,
  branchInfo: {},
  branchesList: [],
  maxRows: 50
};

class BranchContainer extends Component {

  constructor() {
    this.state = initialState;

    bindAll(this, 'openModuleModal', 'closeModuleModal', 'onStatusChange', 'updateSelectedModules', 'updateDownstreamModules', 'updateResetCache', 'updateTriggerInterProjectBuild', 'triggerBuild');
  }

  componentDidMount() {
    this.setup(this.props.params);
  }

  componentWillReceiveProps(nextprops) {
    this.tearDown();
    this.setup(nextprops.params);
    this.setState(initialState);
  }

  componentWillUnmount() {
    this.tearDown();
  }

  onStatusChange(state) {
    this.setState(state);
  }

  setup(params) {
    this.unsubscribeFromBranch = BranchStore.listen(this.onStatusChange);
    this.unsubscribeFromRepo = RepoStore.listen(this.onStatusChange);
    this.unsubscribeFromStars = StarStore.listen(this.onStatusChange);
    StarActions.loadStars('repoBuildContainer');
    BranchActions.loadBranchBuildHistory(params);
    BranchActions.loadBranchInfo(params);
    BranchActions.loadBranchModules(params);
    BranchActions.loadMalformedFiles(params);
    BranchActions.startPolling(params);
    this.tryLoadBranches();
    this.setScrollListener();
  }

  tearDown() {
    BranchActions.stopPolling();
    this.unsubscribeFromStars();
    this.unsubscribeFromBranch();
    this.unsubscribeFromRepo();
    $(window).unbind('scroll');
  }

  tryLoadBranches() {
    if (!this.state || !this.state.branchInfo.repositoryId) {
      setTimeout(this.tryLoadBranches.bind(this), 200);
      return;
    }

    RepoActions.loadBranches(this.state.branchInfo.repositoryId);
  }

  setScrollListener() {
    $(window).scroll($.proxy(function() {
      const scrolledToBottom = $(window).scrollTop() + $(window).height() == $(document).height();

      if (scrolledToBottom) {
        this.setState({
          maxRows: this.state.maxRows + 50
        });
      }
    }, this));
  }

  getClassNames() {
    return classNames([
      'branch-container',
      this.state.builds === null || this.state.maxRows >= this.state.builds.size ? 'normal-padding' : ''
    ]);
  }

  refreshBranches() {
    this.tryLoadBranches();
  }

  openModuleModal() {
    this.setState({
      showModuleModal: true
    });
  }

  closeModuleModal() {
    this.setState({
      showModuleModal: false
    });
  }

  updateSelectedModules(modules) {
    this.setState({
      selectedModules: modules
    });
  }

  updateDownstreamModules(enumValue) {
    this.setState({
      buildDownstreamModules: enumValue
    });
  }

  updateResetCache() {
    this.setState({
      resetCache: !this.state.resetCache
    })
  }

  updateTriggerInterProjectBuild() {
    this.setState({
      triggerInterProjectBuild: !this.state.triggerInterProjectBuild
    })
  }


  triggerBuild() {
    let newState = this.state;

    if (this.state.selectedModules.length === 0) {
      newState.selectedModules = this.state.modules.map((module) => {
        return module.id;
      });
    }

    if (this.state.triggerInterProjectBuild) {
      InterProjectActions.triggerInterProjectBuild(this.props.params, newState);
    } else {
      BranchActions.triggerBuild(this.props.params, newState);
    }
  }

  isLoading() {
    return this.state.loadingBranches
      || this.state.loadingStars
      || this.state.loadingModules
      || this.state.loadingRepo;
  }

  buildDocumentTitle() {
    const {branchInfo} = this.state;

    return branchInfo ? branchInfo.repository + ' - ' + branchInfo.branch : 'Branch Build History';
  }

  renderTable() {
    if (this.state.error) {
      return (
        <GenericErrorMessage
          message={this.state.error}
        />
      );
    }

    else {
      if (this.state.builds) {
        this.props.params.prevBuildState = getPreviousBuildState(this.state.builds);
      }

      return (
        <BranchBuildHistoryTable
          data={this.state.builds !== null ? this.state.builds.slice(0, this.state.maxRows) : null}
          loading={this.isLoading()}
          {...this.state}
          {...this.props}
        />
      );
    }

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

  renderBuildSettingsButton() {
    if (this.isLoading()) {
      return;
    }

    const buildSettingsLink = `/settings/branch/${this.props.params.branchId}`;

    return (
      <Link to={buildSettingsLink}>
        <Button id='build-settings-button'>
            Build Settings
        </Button>
      </Link>
    );
  }

  renderLoader() {
    const showedAllRows = this.state.builds === null || this.state.maxRows >= this.state.builds.size;

    if (this.isLoading() || showedAllRows) {
      return;
    }

    return (
      <Loader align='center' />
    );
  }

  render() {
    return (
      <PageContainer documentTitle={this.buildDocumentTitle()} classNames={this.getClassNames()}>
        {this.renderMalformedFileAlert()}
        <UIGrid>
          <UIGridItem size={7}>
            <BranchHeadline
              loading={this.isLoading()}
              branchInfo={this.state.branchInfo}
              branchesList={this.state.branchesList}
              refreshBranches={this.refreshBranches.bind(this)}
              {...this.state}
              {...this.props}
            />
          </UIGridItem>
          <UIGridItem style={{'paddingTop': '32px'}} size={5} align='RIGHT'>
            <BuildButton
              openModuleModal={this.openModuleModal}
              loading={this.isLoading()}
              error={this.state.error}
            />
            <ModuleModal
              loadingModules={this.isLoading()}
              showModal={this.state.showModuleModal}
              closeModal={this.closeModuleModal}
              triggerBuild={this.triggerBuild}
              onSelectUpdate={this.updateSelectedModules}
              onCheckboxUpdate={this.updateDownstreamModules}
              onResetCacheUpdate={this.updateResetCache}
              onTriggerInterProjectBuild={this.updateTriggerInterProjectBuild}
              modules={this.state.modules}
            />
            {this.renderBuildSettingsButton()}
          </UIGridItem>
        </UIGrid>
        <UIGrid>
          <UIGridItem size={12}>
            {this.renderTable()}
          </UIGridItem>
        </UIGrid>
        {this.renderLoader()}
      </PageContainer>
    );
  }
}


BranchContainer.propTypes = {
  params: PropTypes.object.isRequired
};

export default BranchContainer;
