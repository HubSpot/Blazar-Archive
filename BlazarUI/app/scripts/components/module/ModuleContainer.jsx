import React, {Component, PropTypes} from 'react';
import Module from './Module.jsx';
import PageContainer from '../shared/PageContainer.jsx';
import BuildHistoryStore from '../../stores/buildHistoryStore';
import BuildHistoryActions from '../../actions/buildHistoryActions';

class ModuleContainer extends Component {

  constructor(props) {
    super(props);

    this.state = {
      buildHistory: [],
      loading: true
    };
  }

  componentDidMount() {
    this.unsubscribe = BuildHistoryStore.listen(this.onStatusChange.bind(this));
    BuildHistoryActions.loadBuildHistory(this.props.params);
  }

  componentWillReceiveProps(nextprops) {
    BuildHistoryActions.loadBuildHistory(nextprops.params);
  }

  componentWillUnmount() {
    BuildHistoryActions.updatePollingStatus(false);
    this.unsubscribe();
  }

  onStatusChange(state) {
    this.setState(state);
  }

  render() {
    return (
      <PageContainer>
        <Module
          params={this.props.params}
          buildHistory={this.state.buildHistory}
          loading={this.state.loading}
        />
      </PageContainer>
    );
  }
}

ModuleContainer.propTypes = {
  params: PropTypes.object.isRequired
};

export default ModuleContainer;
