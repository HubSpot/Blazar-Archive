import React, {Component, PropTypes} from 'react';
import BuildStates from '../../constants/BuildStates';
import Build from './Build.jsx';
import PageContainer from '../shared/PageContainer.jsx';
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

  componentWillReceiveProps(nextprops) {
    this.state.loading = true;
    BuildActions.loadBuild(nextprops.params);
  }

  componentWillUnmount() {
    this.unsubscribe();
  }

  onStatusChange(state) {
    if (state.loading) {
      this.setState({
        loading: true
      });
      return;
    }

    if (state.build) {
      this.setState({
        loading: false,
        build: state.build.build,
        log: state.build.log
      });

      const buildState = state.build.build.build.state

      // to do - move polling into action
      if (buildState === BuildStates.IN_PROGRESS || buildState === BuildStates.QUEUED || buildState === BuildStates.LAUNCHING) {
        setTimeout( () => {
          BuildActions.reloadBuild(this.props.params);
        }, 5000);
      }
    } else {
      this.setState({
        loading: false,
        log: state.error
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
