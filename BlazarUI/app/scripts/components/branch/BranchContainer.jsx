import React, {Component, PropTypes} from 'react';
import {bindAll} from 'underscore';
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
import MalformedFileNotification from './MalformedFileNotification.jsx';
import Immutable from 'immutable';

import StarStore from '../../stores/starStore';
import StarActions from '../../actions/starActions';

import BranchStore from '../../stores/branchStore';
import BranchActions from '../../actions/branchActions';

import {getPreviousBuildState} from '../Helpers.js';

let initialState = {
  builds: null,
  stars: [],
  loadingBranches: true,
  loadingStars: true,
  loadingModules: true,
  loadingMalformedFiles: true,
  malformedFiles: [],
  showModuleModal: false,
  modules: Immutable.List.of(),
  selectedModules: [],
  buildDownstreamModules: 'WITHIN_REPOSITORY'
};

class BranchContainer extends Component {

  constructor() {
    this.state = initialState;

    bindAll(this, 'openModuleModal', 'closeModuleModal', 'onStatusChange', 'updateSelectedModules', 'updateDownstreamModules', 'triggerBuild');
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
    this.unsubscribeFromStars = StarStore.listen(this.onStatusChange);
    StarActions.loadStars('repoBuildContainer');
    BranchActions.loadBranchBuilds(params);
    BranchActions.loadModules();
    BranchActions.loadMalformedFiles();
  }
  
  tearDown() {
    BranchActions.stopPolling();
    this.unsubscribeFromStars();
    this.unsubscribeFromBranch();
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

  triggerBuild() {
    BranchActions.triggerBuild(this.state.selectedModules, this.state.buildDownstreamModules);
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
          data={this.state.builds}
          loading={this.state.loadingBranches}
          {...this.state}
          {...this.props}
        />
      );
    }
    
  }

  renderMalformedFileAlert() {
    if (this.state.loadingMalformedFiles) {
      return (<div />);
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

  render() {
    return (
      <PageContainer>
        {this.renderMalformedFileAlert()}
        <UIGrid>
          <UIGridItem size={10}>
            <BranchHeadline
              loading={this.state.loadingStars || this.state.loadingBranches}
              {...this.state}
              {...this.props}
            />
            </UIGridItem>
            <UIGridItem size={2} align='RIGHT'>
              <BuildButton 
                openModuleModal={this.openModuleModal}
                loading={this.state.loadingBranches}
                error={this.state.error}
              />
              <ModuleModal
                loadingModules={this.state.loadingModules}
                showModal={this.state.showModuleModal}
                closeModal={this.closeModuleModal}
                triggerBuild={this.triggerBuild}
                onSelectUpdate={this.updateSelectedModules}
                onCheckboxUpdate={this.updateDownstreamModules}
                modules={this.state.modules}
              />
            </UIGridItem>
            <UIGrid>
              <UIGridItem size={12}>
                {this.renderTable()}
              </UIGridItem>
            </UIGrid>
        </UIGrid>
      </PageContainer>
    );
  }
}


BranchContainer.propTypes = {
  params: PropTypes.object.isRequired
};

export default BranchContainer;
