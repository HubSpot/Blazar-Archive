import React, {Component, PropTypes} from 'react';
import {fromJS} from 'immutable';
import Dashboard from './Dashboard.jsx';
import PageContainer from '../shared/PageContainer.jsx';

import BuildsStore from '../../stores/buildsStore';
import BuildsActions from '../../actions/buildsActions';

import {sortBranchesByTimestamp} from '../Helpers.js';

class DashboardContainer extends Component {

  constructor(props) {
    super(props);

    this.onStatusChange = this.onStatusChange.bind(this);

    this.state = {
      builds: {
        all: [],
        building: [],
        starred: []
      },
      loading: true
    };
  }

  componentDidMount() {
    this.unsubscribeFromBuilds = BuildsStore.listen(this.onStatusChange);
    BuildsActions.loadBuilds(this.props.params);
  }

  componentWillUnmount() {
    BuildsActions.stopPollingBuilds();
    this.unsubscribeFromBuilds();
  }


  onStatusChange(state) {
    this.setState(state);
  }

  render() {
    const activeStarredBuilds = this.state.builds.starred.filter((build) => build.gitInfo.active);
    return (
      <PageContainer documentTitle="Dashboard" classNames="page-dashboard">
        <Dashboard
          starredBuilds={fromJS(sortBranchesByTimestamp(activeStarredBuilds, false))}
          loading={this.state.loading}
          params={this.props.params}
        />
      </PageContainer>
    );
  }
}

DashboardContainer.propTypes = {
  params: PropTypes.object.isRequired
};

export default DashboardContainer;
