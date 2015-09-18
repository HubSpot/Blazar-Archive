import React, {Component, PropTypes} from 'react';
import Module from './Module.jsx';
import PageContainer from '../shared/PageContainer.jsx';
import BuildHistoryStore from '../../stores/buildHistoryStore';
import BuildStore from '../../stores/buildStore';
import BuildHistoryActions from '../../actions/buildHistoryActions';
import BuildActions from '../../actions/buildActions';
import {bindAll} from 'underscore';

class ModuleContainer extends Component {

  constructor(props) {
    super(props);
    bindAll(this, 'triggerBuild', 'onStatusChange');

    this.state = {
      buildHistory: [],
      loading: true,
      buildTriggeringDone: true,
      buildTriggeringError: ''
    };
  }

  componentDidMount() {
    this.unsubscribeFromBuildHistory = BuildHistoryStore.listen(this.onStatusChange);
    this.unsubscribeFromBuild = BuildStore.listen(this.onStatusChange);

    BuildHistoryActions.loadBuildHistory(this.props.params);    
  }

  componentWillReceiveProps(nextprops) {
    BuildHistoryActions.loadBuildHistory(nextprops.params);
  }

  componentWillUnmount() {
    BuildHistoryActions.updatePollingStatus(false);
    this.unsubscribeFromBuildHistory();
    this.unsubscribeFromBuild();
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
        <Module
          params={this.props.params}
          buildHistory={this.state.buildHistory}
          loading={this.state.loading}
          triggerBuild={this.triggerBuild}
          buildTriggering={!this.state.buildTriggeringDone}
        />
      </PageContainer>
    );
  }
}

ModuleContainer.propTypes = {
  params: PropTypes.object.isRequired
};

export default ModuleContainer;
