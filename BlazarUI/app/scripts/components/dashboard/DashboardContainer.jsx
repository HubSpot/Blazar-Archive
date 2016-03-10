import React, {Component} from 'react';
import {contains, pluck, isEqual, filter} from 'underscore';
import Immutable, {fromJS} from 'immutable'
import Dashboard from './Dashboard.jsx';
import PageContainer from '../shared/PageContainer.jsx';

import BuildsStore from '../../stores/buildsStore';

import StarActions from '../../actions/starActions';
import StarStore from '../../stores/starStore';

class DashboardContainer extends Component {

  constructor(props) {
    super(props);

    this.onStatusChange = this.onStatusChange.bind(this);

    this.state = {
      stars: Immutable.List.of(),
      builds: Immutable.List.of(),
      starredBuilds: [],
      loadingStars: true,
      loading: true
    }
  }

  componentDidMount() {
    this.unsubscribeFromBuilds = BuildsStore.listen(this.onStatusChange);
    this.unsubscribeFromStars = StarStore.listen(this.onStatusChange);

    StarActions.loadStars();
  }

  componentWillUnmount() {
    this.unsubscribeFromBuilds();
    this.unsubscribeFromStars();
  }

  checkStarHistory() {
    if (this.state.stars.size === 0) {
      return;
    }

    if (this.state.builds.all) {
      const starredBuilds = this.state.builds.all.filter((build) => {
        return contains(this.state.stars, build.gitInfo.id);
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
          starredBuilds={fromJS(this.state.starredBuilds)}
          loadingStars={this.state.loadingStars}
          loadingBuilds={this.state.loading}
          params={this.props.params}
        />
      </PageContainer>
    );
  }
}

export default DashboardContainer;
