import React, {Component, PropTypes} from 'react';
import {bindAll, contains, some} from 'underscore';
import {ACTIVE_BUILD_STATES} from '../../constants/ActiveBuildStates.js';
import {BuildStates} from '../../constants/BuildStates.js';
import {getIsStarredState} from '../Helpers.js';
import Build from './Build.jsx';
import PageContainer from '../shared/PageContainer.jsx';

import BuildStore from '../../stores/buildStore';
import BuildActions from '../../actions/buildActions';
import StarStore from '../../stores/starStore';
import StarActions from '../../actions/starActions';
import LocationStore from '../../stores/locationStore';

class BuildContainer extends Component {

  constructor(props) {
    super(props);

    bindAll(this, 'toggleStar');

    this.state = {
      loading: true,
      build: {
        buildState: {},
        gitInfo: {},
        module: { name: ''}
      },
      log: '',
      stars: [],
    };
  }

  componentDidMount() {
    this.unsubscribeFromBuild = BuildStore.listen(this.onStatusChange.bind(this));
    this.unsubscribeFromStars = StarStore.listen(this.onStatusChange.bind(this));
    BuildActions.loadBuild(this.props.params);
    StarActions.loadStars();
  }

  componentWillReceiveProps(nextprops) {
    console.log('will receive da props');
    this.setState({
      loading: true
    });
    BuildActions.loadBuild(nextprops.params);
  }

  componentWillUnmount() {
    this.unsubscribeFromBuild();
    this.unsubscribeFromStars();
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
        log: state.build.log
      });

      if (contains(ACTIVE_BUILD_STATES, state.build.build.build.state)){
        setTimeout( () => {
          BuildActions.reloadBuild(this.props.params);
        }, 5000);
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
          params={this.props.params}
          loading={this.state.loading}
          pathname={LocationStore.pathname}
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
