import React, {Component, PropTypes} from 'react';
import {bindAll, contains, some, clone} from 'underscore';
import BuildStates from '../../constants/BuildStates.js';
import {getIsStarredState} from '../Helpers.js';
import Build from './Build.jsx';
import PageContainer from '../shared/PageContainer.jsx';
import {getPathname} from '../Helpers';

import BuildStore from '../../stores/buildStore';
import BuildActions from '../../actions/buildActions';
import StarStore from '../../stores/starStore';
import StarActions from '../../actions/starActions';
import LocationStore from '../../stores/locationStore';

const initialState = {
  loading: true,
  build: {
    buildState: {},
    gitInfo: {},
    module: { name: ''}
  },
  log: '',
  fetchingLog: true,
  stars: [],
};

class BuildContainer extends Component {

  constructor(props) {
    super(props);
    bindAll(this, 'toggleStar');
    this.state = initialState;
  }

  componentDidMount() {
    this.setup(this.props);
  }

  setup(props) {
    this.originalParams = clone(props.params);
    this.unsubscribeFromBuild = BuildStore.listen(this.onStatusChange.bind(this));
    this.unsubscribeFromStars = StarStore.listen(this.onStatusChange.bind(this));
    BuildActions.loadBuild(props.params);
    StarActions.loadStars();
  }

  tearDown() {
    this.unsubscribeFromBuild();
    this.unsubscribeFromStars();
    BuildActions.stopWatchingBuild(this.props.params.moduleId)
  }

  componentWillReceiveProps(nextProps) {
    this.tearDown();
    this.setup(nextProps);
    this.setState(initialState);
  }

  componentWillUnmount() {
    this.tearDown()
  }

  onStatusChange(state) {

    if (state.loading) {
      this.setState({
        loading: true
      });
      return;
    }

    if (state.stars) {
      this.setState({
        stars: state.stars
      });
    }

    if (state.build) {
      this.setState({
        loading: false,
        build: state.build.build,
        log: state.build.log,
        fetchingLog: state.build.fetchingLog
      });

      if (contains([BuildStates.QUEUED, BuildStates.LAUNCHING], state.build.build.build.state)){
        setTimeout( () => {
          BuildActions.reloadBuild(this.props.params);
        }, 2000);
      }
    }
  }

  toggleStar(isStarred, starInfo) {
    StarActions.toggleStar(isStarred, starInfo);
  }

  render() {
    return (
      <PageContainer>
        <Build
          build={this.state.build}
          log={this.state.log}
          fetchingLog={this.state.fetchingLog}
          originalParams={this.originalParams}
          params={this.props.params}
          loading={this.state.loading}
          pathname={getPathname()}
          toggleStar={this.toggleStar}
          isStarred={getIsStarredState(this.state.stars, this.props.params.moduleId)}
        />
      </PageContainer>
    );
  }
}

BuildContainer.propTypes = {
  params: PropTypes.object.isRequired
};

export default BuildContainer;
