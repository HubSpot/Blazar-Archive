import React, {Component} from 'react';
import {contains, pluck, isEqual, filter} from 'underscore';
import Immutable, {fromJS} from 'immutable'
import Dashboard from './Dashboard.jsx';
import PageContainer from '../shared/PageContainer.jsx';

import BuildsStore from '../../stores/buildsStore';
import BuildsActions from '../../actions/buildsActions';

import StarActions from '../../actions/starActions';
import StarStore from '../../stores/starStore';

import {sortBranchesByTimestamp} from '../Helpers.js';

class DashboardContainer extends Component {

  constructor(props) {
    super(props);

    this.onStatusChange = this.onStatusChange.bind(this);

    this.state = {
      stars: Immutable.List.of(),
      dashboardBuilds: Immutable.List.of(),
      starredBuilds: [],
      loadingStars: true,
      loadingDashboardBuilds: true
    }
  }

  componentDidMount() {
    this.unsubscribeFromBuilds = BuildsStore.listen(this.onStatusChange);
    this.unsubscribeFromStars = StarStore.listen(this.onStatusChange);

    StarActions.loadStars();
    BuildsActions.loadBuildsForDashboard();
  }

  componentWillUnmount() {
    this.unsubscribeFromBuilds();
    this.unsubscribeFromStars();
  }

  checkStarHistory() {
    if (this.state.stars.size === 0) {
      return;
    }

    if (this.state.dashboardBuilds.all) {
      const starredBuilds = this.state.dashboardBuilds.all.filter((build) => {
        return contains(this.state.stars, build.gitInfo.id)
          && build.gitInfo.active;
      });

      this.setState({ starredBuilds: starredBuilds });
    }
  }

  onStatusChange(state) {
    this.setState(state);
    this.checkStarHistory();
  }

  render() {
    return (
      <PageContainer classNames='page-dashboard'>
        <Dashboard 
          starredBuilds={fromJS(sortBranchesByTimestamp(this.state.starredBuilds, false))}
          loadingStars={this.state.loadingStars}
          loadingBuilds={this.state.loadingDashboardBuilds}
          params={this.props.params}
        />
      </PageContainer>
    );
  }
}

export default DashboardContainer;
