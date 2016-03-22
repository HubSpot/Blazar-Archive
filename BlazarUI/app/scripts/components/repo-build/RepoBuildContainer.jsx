import React, {Component, PropTypes} from 'react';
import {bindAll, clone, some} from 'underscore';

import PageContainer from '../shared/PageContainer.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import GenericErrorMessage from '../shared/GenericErrorMessage.jsx';

import StarStore from '../../stores/starStore';
import RepoBuildStore from '../../stores/repoBuildStore';
import StarActions from '../../actions/starActions';
import RepoBuildActions from '../../actions/repoBuildActions';

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
  loadingStars: true
};

class RepoBuildContainer extends Component {

  constructor(props) {
    super(props);
    this.state = initialState;

    bindAll(this, 'onStatusChange')
  }
  
  componentDidMount() {
    this.setup(this.props.params);
  }

  componentWillReceiveProps(nextprops) {
    this.tearDown()
    this.setup(nextprops.params);
    this.setState(initialState);
  }

  componentWillUnmount() {
    this.tearDown()
  }
  
  setup(params) { 
    this.unsubscribeFromStars = StarStore.listen(this.onStatusChange);
    this.unsubscribeFromRepoBuild = RepoBuildStore.listen(this.onStatusChange);
    StarActions.loadStars('repoBuildContainer');
    RepoBuildActions.loadModuleBuilds(params);
    RepoBuildActions.loadMalformedFiles();
  }

  tearDown() {
    RepoBuildActions.stopPolling();
    this.unsubscribeFromStars();
    this.unsubscribeFromRepoBuild();
  }
  
  onStatusChange(state) {
    this.setState(state);
  }
  
  // to do
  triggerCancelBuild() {
    RepoBuildActions.cancelBuild();
  }
  
  triggerBuild() {
    RepoBuildActions.triggerBuild();
  }

  renderSectionContent() {
    if (this.state.error) {
      return this.renderError();
    }

    else {
      return this.renderPage();
    }
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
              loading={this.state.loadingStars || this.state.loadingModuleBuilds}
            />
          </UIGridItem>
        </UIGrid>
        <UIGridItem size={12}>
          <RepoBuildDetail 
            {...this.props}
            {...this.state}
            loading={this.state.loadingModuleBuilds}
            triggerCancelBuild={this.triggerCancelBuild}
          />
          <RepoBuildModulesTable
            params={this.props.params}
            data={this.state.moduleBuilds ? this.state.moduleBuilds.toJS() : []}
            currentRepoBuild={this.state.currentRepoBuild}
            loading={this.state.loadingModuleBuilds}
            {...this.state}
          />
        </UIGridItem>
      </div>
    );
  }

  render() {
    return (
      <PageContainer>
        {this.renderSectionContent()}
      </PageContainer>
    );
  }
}

RepoBuildContainer.propTypes = {
  params: PropTypes.object.isRequired
};

export default RepoBuildContainer;
