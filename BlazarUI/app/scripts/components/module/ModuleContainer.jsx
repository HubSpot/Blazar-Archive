import React, {Component, PropTypes} from 'react';
import {bindAll, clone, some} from 'underscore';
import {getPathname} from '../Helpers';
import PageContainer from '../shared/PageContainer.jsx';
import {getIsStarredState} from '../Helpers.js';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import BuildHistoryTable from './BuildHistoryTable.jsx';
import Headline from '../shared/headline/Headline.jsx';
import HeadlineDetail from '../shared/headline/HeadlineDetail.jsx';
import Icon from '../shared/Icon.jsx';
import BuildButton from './BuildButton.jsx';
import Star from '../shared/Star.jsx';

import BuildHistoryStore from '../../stores/buildHistoryStore';
import BuildStore from '../../stores/buildStore';
import BuildHistoryActions from '../../actions/buildHistoryActions';
import BuildActions from '../../actions/buildActions';
import StarStore from '../../stores/starStore';
import StarActions from '../../actions/starActions';
import LocationStore from '../../stores/locationStore';

class ModuleContainer extends Component {

  constructor(props) {
    super(props);
    bindAll(this, 'triggerBuild', 'onStatusChange', 'toggleStar');

    this.state = {
      buildHistory: [],
      stars: [],
      buildTriggeringDone: true,
      loadingHistory: true,
      loadingStars: true,
      buildTriggeringError: ''
    };
  }

  componentDidMount() {
    this.unsubscribeFromBuildHistory = BuildHistoryStore.listen(this.onStatusChange);
    this.unsubscribeFromBuild = BuildStore.listen(this.onStatusChange);
    this.unsubscribeFromStars = StarStore.listen(this.onStatusChange.bind(this));

    BuildHistoryActions.loadBuildHistory(this.props.params);
    StarActions.loadStars();
  }

  componentWillReceiveProps(nextprops) {
    BuildHistoryActions.loadBuildHistory(nextprops.params);
    this.setState({
      loadingStars: true,
      loadingHistory: true
    });
  }

  componentWillUnmount() {
    BuildHistoryActions.updatePollingStatus(false);
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

  toggleStar(isStarred, starInfo) {
    StarActions.toggleStar(isStarred, starInfo);
  }

  render() {
    return (
      <PageContainer>
        <UIGrid>
          <UIGridItem size={10}>
            <Headline>
              <Star
                className='icon-roomy'
                isStarred={getIsStarredState(this.state.stars, this.props.params.moduleId)}
                toggleStar={this.toggleStar}
                modulePath={getPathname()}
                moduleName={this.props.params.module}
                moduleId={this.props.params.moduleId}
                updateWithState={true}
              />
              {this.props.params.module} 
              <HeadlineDetail>
                Build History
              </HeadlineDetail>
            </Headline>
          </UIGridItem>           
          <UIGridItem size={2} align='RIGHT'>
            <BuildButton 
              triggerBuild={this.triggerBuild} 
              loading={!this.state.loadingStars || !this.state.loadingHistory}
            />
          </UIGridItem>
          <UIGridItem size={12}>
            <BuildHistoryTable
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
