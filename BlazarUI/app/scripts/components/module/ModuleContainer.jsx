import React from 'react';
import Module from './Module.jsx';
import PageContainer from '../layout/PageContainer.jsx';
import BuildHistoryStore from '../../stores/buildHistoryStore';
import BuildHistoryActions from '../../actions/buildHistoryActions';

class ModuleContainer extends React.Component {

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

  componentWillUnmount() {
    this.unsubscribe();
  }

  onStatusChange(state) {
    this.setState(state);
  }

  componentWillReceiveProps(nextprops) {
    BuildHistoryActions.loadBuildHistory(nextprops.params);
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
  params: React.PropTypes.object.isRequired
};

export default ModuleContainer;
