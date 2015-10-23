import React, {Component, PropTypes} from 'react';
import {bindAll, clone, some} from 'underscore';
import ModuleHeadline from './ModuleHeadline.jsx';
import PageContainer from '../shared/PageContainer.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import BuildHistoryTable from './BuildHistoryTable.jsx';
import BuildButton from './BuildButton.jsx';

import BuildHistoryStore from '../../stores/buildHistoryStore';
import BuildStore from '../../stores/buildStore';
import BuildHistoryActions from '../../actions/buildHistoryActions';
import BuildActions from '../../actions/buildActions';
import StarStore from '../../stores/starStore';
import StarActions from '../../actions/starActions';
import LocationStore from '../../stores/locationStore';

let initialState = {
  buildHistory: [],
  stars: [],
  loadingHistory: true,
  loadingStars: true,
  buildTriggeringError: ''
};

class ModuleContainer extends Component {

  constructor(props) {
    super(props);
    bindAll(this, 'triggerBuild', 'onStatusChange');
    this.state = initialState;
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
    BuildHistoryActions.updatePollingStatus(false);
    this.tearDown()
  }
  
  setup(params) {
    this.unsubscribeFromBuildHistory = BuildHistoryStore.listen(this.onStatusChange);
    this.unsubscribeFromBuild = BuildStore.listen(this.onStatusChange);  
    this.unsubscribeFromStars = StarStore.listen(this.onStatusChange.bind(this));
  
    BuildHistoryActions.loadBuildHistory(params);
    StarActions.loadStars('moduleContainer');
  }
  
  tearDown() {
    this.unsubscribeFromBuildHistory();
    this.unsubscribeFromBuild();
    this.unsubscribeFromStars();
  }

  onStatusChange(state) {
    this.setState(state);
  }

  triggerBuild() {
    BuildActions.triggerBuild(this.props.params.moduleId);
  }

  render() {
    return (
      <PageContainer>
        <UIGrid>
          <UIGridItem size={10}>
            <ModuleHeadline
              params={this.props.params}
              stars={this.state.stars}
              loading={this.state.loadingStars || this.state.loadingHistory}
            />
          </UIGridItem>           
          <UIGridItem size={2} align='RIGHT'>
            <BuildButton 
              triggerBuild={this.triggerBuild} 
              loading={this.state.loadingHistory}
            />
          </UIGridItem>
          <UIGridItem size={12}>
            <BuildHistoryTable
              params={this.props.params}
              buildHistory={this.state.buildHistory}
              loading={this.state.loadingStars || this.state.loadingHistory}
            />
          </UIGridItem>
        </UIGrid>
      </PageContainer>
    );
  }
}

ModuleContainer.propTypes = {
  params: PropTypes.object.isRequired
};

export default ModuleContainer;
