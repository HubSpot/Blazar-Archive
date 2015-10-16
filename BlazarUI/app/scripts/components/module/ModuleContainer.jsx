import React, {Component, PropTypes} from 'react';
import {bindAll, clone} from 'underscore';
import {getPathname} from '../Helpers';

import Module from './Module.jsx';
import PageContainer from '../shared/PageContainer.jsx';
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
        <Module
          params={this.props.params}
          pathname={getPathname()}
          buildHistory={this.state.buildHistory}
          stars={this.state.stars}
          loadingStars={this.state.loadingStars}
          loadingHistory={this.state.loadingHistory}
          triggerBuild={this.triggerBuild}
          toggleStar={this.toggleStar}
        />
      </PageContainer>
    );
  }
}

ModuleContainer.propTypes = {
  params: PropTypes.object.isRequired
};

export default ModuleContainer;
