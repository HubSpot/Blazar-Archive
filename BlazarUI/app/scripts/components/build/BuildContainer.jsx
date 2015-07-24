import React, {Component, PropTypes} from 'react';
import Build from './Build.jsx';
import PageContainer from '../layout/PageContainer.jsx';

import BuildStore from '../../stores/buildStore';
import BuildActions from '../../actions/buildActions';

class BuildContainer extends Component {

  constructor(props) {
    super(props);

    this.state = {
      loading: true,
      build: {
        buildState: {},
        gitInfo: {},
        module: { name: ''}
      },
      log: ''
    };
  }

  componentDidMount() {
    this.unsubscribe = BuildStore.listen(this.onStatusChange.bind(this));
    BuildActions.loadBuild(this.props.params);
  }

  componentWillUnmount() {
    this.unsubscribe();
  }

  componentWillReceiveProps(nextprops) {
    BuildActions.loadBuild(nextprops.params);
  }

  onStatusChange(state) {

    if (state.build) {
      this.setState({
        loading: false,
        build: state.build.build,
        log: state.build.log
      });
    }

  }

  render() {
    return (
      <PageContainer>
        <Build
          build={this.state.build}
          log={this.state.log}
          params={this.props.params}
          loading={this.state.loading}
        />
      </PageContainer>
    );
  }
}

BuildContainer.propTypes = {
  params: PropTypes.object.isRequired
};

export default BuildContainer;
