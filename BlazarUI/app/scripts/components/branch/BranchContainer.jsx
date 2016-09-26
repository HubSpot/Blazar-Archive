import React, {Component, PropTypes} from 'react';
import { connect } from 'react-redux';
import {Link} from 'react-router';
import {Button} from 'react-bootstrap';
import {bindAll} from 'underscore';
import classNames from 'classnames';
import PageContainer from '../shared/PageContainer.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import BranchBuildHistoryTable from './BranchBuildHistoryTable.jsx';
import BranchHeadline from './BranchHeadline.jsx';
import Loader from '../shared/Loader.jsx';
import GenericErrorMessage from '../shared/GenericErrorMessage.jsx';
import BuildButton from './BuildButton.jsx';
import BuildBranchModalContainer from '../shared/BuildBranchModalContainer.jsx';
import MalformedFileNotification from '../shared/MalformedFileNotification.jsx';
import $ from 'jquery';

import BranchStore from '../../stores/branchStore';
import BranchActions from '../../actions/branchActions';
import RepoStore from '../../stores/repoStore';
import RepoActions from '../../actions/repoActions';
import { showBuildBranchModal } from '../../redux-actions/buildBranchFormActions';

import {getPreviousBuildState} from '../Helpers.js';

const initialState = {
  builds: null,
  loadingBranches: true,
  loadingModules: true,
  loadingMalformedFiles: true,
  loadingRepo: true,
  malformedFiles: [],
  modules: [],
  branchId: 0,
  branchInfo: {},
  branchesList: [],
  maxRows: 50
};

class BranchContainer extends Component {

  constructor() {
    this.state = initialState;
    bindAll(this, 'onStatusChange', 'refreshBranches');
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
    $(window).scroll($.proxy(() => {
      const scrolledToBottom = $(window).scrollTop() + $(window).height() === $(document).height();

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

  isLoading() {
    return this.state.loadingBranches
      || this.state.loadingModules
      || this.state.loadingRepo;
  }

  buildDocumentTitle() {
    const {branchInfo} = this.state;

    return branchInfo ? `${branchInfo.repository}-${branchInfo.branch}` : 'Branch Build History';
  }

  renderTable() {
    const error = this.props.branchBuildError || this.state.error;
    if (error) {
      return (
        <GenericErrorMessage message={error} />
      );
    }

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

  renderMalformedFileAlert() {
    if (this.state.loadingMalformedFiles || this.state.malformedFiles.length === 0) {
      return null;
    }

    return (
      <UIGrid>
        <UIGridItem size={12}>
          <MalformedFileNotification
            loading={this.state.loadingMalformedFiles}
            malformedFiles={this.state.malformedFiles}
          />
        </UIGridItem>
      </UIGrid>
    );
  }

  renderBuildSettingsButton() {
    if (this.isLoading()) {
      return null;
    }

    const buildSettingsLink = `/settings/branch/${this.props.params.branchId}`;

    return (
      <Link to={buildSettingsLink}>
        <Button id="build-settings-button">
            Build Settings
        </Button>
      </Link>
    );
  }

  renderLoader() {
    const showedAllRows = this.state.builds === null || this.state.maxRows >= this.state.builds.size;

    if (this.isLoading() || showedAllRows) {
      return null;
    }

    return <Loader align="center" />;
  }

  render() {
    return (
      <PageContainer documentTitle={this.buildDocumentTitle()} classNames={this.getClassNames()}>
        <UIGrid>
          <UIGridItem size={7}>
            <BranchHeadline
              loading={this.isLoading()}
              branchInfo={this.state.branchInfo}
              branchesList={this.state.branchesList}
              refreshBranches={this.refreshBranches}
              {...this.state}
              {...this.props}
            />
          </UIGridItem>
          <UIGridItem style={{'paddingTop': '32px'}} size={5} align="RIGHT">
            <BuildButton
              openBuildBranchModal={this.props.showBuildBranchModal}
              loading={this.isLoading()}
            />
            <BuildBranchModalContainer
              branchId={this.props.params.branchId}
              modules={this.state.modules}
              onBuildStart={() => {BranchActions.loadBranchBuildHistory(this.props.params);}}
            />
            {this.renderBuildSettingsButton()}
          </UIGridItem>
        </UIGrid>
        {this.renderMalformedFileAlert()}
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
  params: PropTypes.object.isRequired,
  showBuildBranchModal: PropTypes.func.isRequired,
  branchBuildError: PropTypes.string
};

const mapStateToProps = (state) => ({
  branchBuildError: state.buildBranchForm.get('error')
});

export default connect(mapStateToProps, {showBuildBranchModal})(BranchContainer);
