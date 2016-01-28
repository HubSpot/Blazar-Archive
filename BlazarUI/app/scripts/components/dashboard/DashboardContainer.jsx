import React, {Component} from 'react';
import {contains, pluck, isEqual, filter} from 'underscore';
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
      stars: [],
      builds: [],
      starredBuilds: [],
      loadingStars: true
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

    //console.log("this.state.builds.all: ", this.state.builds.all);

    if (this.state.builds.size !== 0) {
      const newStarredBuilds = this.state.builds.all.filter((build) => {
        return contains(this.state.stars, build.get('gitInfo').get('id'));
      });

      //console.log("newStarredBuilds: ", newStarredBuilds.toString());

      this.setState({ starredBuilds: newStarredBuilds });
    }
  }

  onStatusChange(state) {
    this.setState(state);
    this.checkStarHistory();

    //console.log("STATE UPDATED: ", this.state);
  }

  render() {
    return (
      <PageContainer classNames='page-dashboard'>
        <Dashboard 
          starredBuilds={this.state.starredBuilds}
          loadingStars={this.stateloadingStars}
          params={this.props.params}
        />
      </PageContainer>
    );
  }
}

export default DashboardContainer;
