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
  log: [],
  navigating: null,
  fetchingLog: false,
  currentOffset: 0,
  currrentOffsetLine: 0,
  stars: [],
  error: false
};

class BuildContainer extends Component {

  constructor(props) {
    super(props);
    bindAll(this, 'toggleStar', 'triggerCancelBuild', 'pageUp', 'navigateLogChange');
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
    StarActions.loadStars('buildContainer');
  }

  tearDown() {
    this.unsubscribeFromBuild();
    this.unsubscribeFromStars();
    BuildActions.stopWatchingBuild(this.props.params.buildId, this.props.params.moduleId)
  }

  componentWillReceiveProps(nextProps) {
    this.tearDown();
    this.setup(nextProps);
    this.setState(initialState);
  }

  componentWillUnmount() {
    this.tearDown()
  }
  
  pageUp() {
    //
    //
    // To Do:
    // Do we need build id???
    // BuildActions.pageUp(this.state.build.build.id, this.props.params.moduleId);
    BuildActions.pageUp(this.props.params.moduleId);
  }
  
  navigateLogChange(position) {
    console.log('navigation change: ', position);
    BuildActions.navigateLogChange(this.props.params.moduleId, position)
    // this.setState({
    //   navigating: position
    // })
  }
  
  triggerCancelBuild(buildId, moduleId) {
    BuildActions.cancelBuild(buildId, moduleId);
  }

  onStatusChange(state) {
    
    let stateUpdate = {}
    
    if (state.loadBuildCancelError) {
      this.setState({
        error: state.loadBuildCancelError
      });
      
      // TO DO
      // stateUpdate.error = state.loadBuildCancelError
    }

    if (state.loading) {
      this.setState({
        loading: true
      });
      
      // stateUpdate.loading = true;
      // this.setState(stateUpdate)
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
        fetchingLog: state.build.fetchingLog,
        
        scrollToOffset: state.build.scrollToOffset,
        currentOffset: state.build.currentOffset,
        currrentOffsetLine: state.build.currrentOffsetLine
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
          error={this.state.error}
          build={this.state.build}
          log={this.state.log}
          navigating={this.state.navigating}
          navigateLogChange={this.navigateLogChange}
          currentOffset={this.state.currentOffset}
          currrentOffsetLine={this.state.currrentOffsetLine}
          scrollToOffset={this.state.scrollToOffset}
          fetchingLog={this.state.fetchingLog}
          pageUp={this.pageUp}
          originalParams={this.originalParams}
          params={this.props.params}
          loading={this.state.loading}
          pathname={getPathname()}
          triggerCancelBuild={this.triggerCancelBuild}
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
