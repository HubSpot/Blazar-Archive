import React, {Component} from 'react';
import {contains, pluck, isEqual} from 'underscore';
import Dashboard from './Dashboard.jsx';
import PageContainer from '../shared/PageContainer.jsx';

import BuildsActions from '../../actions/buildsActions';
import BuildsStore from '../../stores/buildsStore';

import StarActions from '../../actions/starActions';
import StarStore from '../../stores/starStore';

import HostsStore from '../../stores/hostsStore';
import HostsActions from '../../actions/hostsActions';

import BuildHistoryActions from '../../actions/buildHistoryActions';
import BuildHistoryStore from '../../stores/buildHistoryStore';

class DashboardContainer extends Component {

  constructor(props) {
    super(props);

    this.onStatusChange = this.onStatusChange.bind(this);

    this.state = {
      builds: [],
      stars: [],
      hosts: [],
      modulesBuildHistory: [],
      loadingModulesBuildHistory: true,
      loadingHosts: true,
      loadingStars: true,
      loading: true
    };

    this.starsHistory = [];
    this.starsHistoryChanged = false;
  }

  componentDidMount() {
    // try:    
    this.unsubscribeFromBuilds = BuildsStore.listen(this.onStatusChange);
    this.unsubscribeFromStars = StarStore.listen(this.onStatusChange);
    this.unsubscribeFromBuildHistory = BuildHistoryStore.listen(this.onStatusChange);
    this.unsubscribeFromHosts = HostsStore.listen(this.onStatusChange);

    StarActions.loadStars();
    HostsActions.loadHosts();
  }

  componentWillUnmount() {
    this.unsubscribeFromBuilds();
    this.unsubscribeFromStars();
    this.unsubscribeFromBuildHistory();
    this.unsubscribeFromHosts();
  }

  // check if star history has changed so we
  // can fetch the updated history
  checkStarHistory() {
    if (this.state.stars.length === 0) {
      return;
    }

    const starIds = pluck(this.state.stars, 'moduleId');
    const starredBuilds = this.state.builds.filter((build) => {
      return contains(starIds, build.module.id);
    });
    
    this.starsHistoryChanged = !isEqual(this.starsHistory, starredBuilds)
    this.starsHistory = starredBuilds;

    if (this.starsHistoryChanged) {
      BuildHistoryActions.loadModulesBuildHistory({
        modules: this.state.stars,
        limit: 3
      });
    }
  }

  onStatusChange(state) {
    this.setState(state);
    this.checkStarHistory();

    const noHistoryToFetch = state.stars && state.stars.length === 0;
    const haveHistory = !this.state.loadingModulesBuildHistory && !this.state.loadingStars;

    if (state.stars){
      BuildHistoryActions.loadModulesBuildHistory({
        modules: state.stars,
        limit: 3
      });
    }

    // now that we have builds and stars, let's render the page
    if (noHistoryToFetch || haveHistory) {
      this.setState({
        loading: false
      });
    }
  }

  render() {
    return (
      <PageContainer classNames='page-dashboard'>
        <Dashboard 
          loading={this.state.loading} 
          modulesBuildHistory={this.state.modulesBuildHistory} 
          params={this.props.params}
          hosts={this.state.hosts}
          loadingHosts={this.state.loadingHosts}
        />
      </PageContainer>
    );
  }
}

export default DashboardContainer;
