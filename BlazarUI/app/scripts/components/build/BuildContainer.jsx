import React, {Component, PropTypes} from 'react';
import {bindAll, some, clone} from 'underscore';
import BuildStates from '../../constants/BuildStates.js';
import {getIsStarredState} from '../Helpers.js';
import Build from './Build.jsx';
import PageContainer from '../shared/PageContainer.jsx';
import {getPathname, buildIsOnDeck} from '../Helpers';

import BuildStore from '../../stores/buildStore';
import BuildActions from '../../actions/buildActions';
import StarStore from '../../stores/starStore';
import StarActions from '../../actions/starActions';
import LocationStore from '../../stores/locationStore';

const initialState = {
  loading: true,
  build: {
    build: {},
    gitInfo: {},
    module: { name: ''}
  },
  log: {
    logLines: [],
    fetchingLog: false,
    currrentOffsetLine: 0,
    positionChange: null
  },
  fetchingLog: false,
  stars: [],
  error: false
};

class BuildContainer extends Component {

  constructor(props) {
    super(props);
    bindAll(this, 
      'toggleStar', 
      'triggerCancelBuild', 
      'pageLog', 
      'changeOffsetWithNavigation',
      'fetchStartOfLog',
      'fetchEndOfLog',
      'shouldPoll'
    );
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

  shouldPoll(state) {
    BuildActions.shouldPoll(this.props.params.moduleId, state);
  }
  
  pageLog(direction) {
    BuildActions.pageLog(this.props.params.moduleId, direction);
  }
  
  // maybe dont need...
  fetchStartOfLog() {
    BuildActions.fetchStartOfLog(this.props.params.moduleId);
  }
  
  fetchEndOfLog(options) {
    console.log('fetch from other ', options);
    
    BuildActions.fetchEndOfLog(this.props.params.moduleId, options);
  }

  changeOffsetWithNavigation(position) {    
    if (position === 'top') {
      BuildActions.fetchStartOfLog(this.props.params.moduleId, {position: position})
    }
    
    else if (position === 'bottom') {
      BuildActions.fetchEndOfLog(this.props.params.moduleId, {position: position, poll: true})
    }
  }
  
  triggerCancelBuild(buildId, moduleId) {
    BuildActions.cancelBuild(buildId, moduleId);
  }

  onStatusChange(state) {
    if (state.error) {
      this.setState({
        loading: false,
        error: state.error
      })
    }

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
        log: state.build.log || initialState.log,
        fetchingLog: state.build.fetchingLog,
      });

      if (buildIsOnDeck(state.build.build.build.state)) {
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
          fetchStartOfLog={this.fetchStartOfLog}
          fetchEndOfLog={this.fetchEndOfLog}
          log={this.state.log}
          fetchingLog={this.state.fetchingLog}
          pageLog={this.pageLog}
          shouldPoll={this.shouldPoll}
          changeOffsetWithNavigation={this.changeOffsetWithNavigation}
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
