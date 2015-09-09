import React, {Component} from 'react';
import Dashboard from './Dashboard.jsx';
import PageContainer from '../shared/PageContainer.jsx';

import BuildsActions from '../../actions/buildsActions';
import BuildsStore from '../../stores/buildsStore';

import StarActions from '../../actions/starActions';
import StarStore from '../../stores/starStore';

import BuildHistoryActions from '../../actions/buildHistoryActions';
import BuildHistoryStore from '../../stores/buildHistoryStore';

class DashboardContainer extends Component {

  constructor(props) {
    super(props);

    this.state = {
      builds: {},
      stars: [], 
      modulesBuildHistory: [],
      loadingModulesBuildHistory: true,
      loadingStars: true,
      loading: true
    };
  }

  componentDidMount() {
    this.unsubscribeFromBuilds = BuildsStore.listen(this.onStatusChange.bind(this));
    this.unsubscribeFromStars = StarStore.listen(this.onStatusChange.bind(this));
    this.unsubscribeFromBuildHistory = BuildHistoryStore.listen(this.onStatusChange.bind(this));

    BuildsActions.loadBuilds();
    StarActions.loadStars();
  }

  componentWillUnmount() {
    this.unsubscribeFromBuilds();
    this.unsubscribeFromStars();
    this.unsubscribeFromBuildHistory();
  }

  onStatusChange(state) {

    this.setState(state);

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
          builds={this.state.builds}
          loading={this.state.loading} 
          modulesBuildHistory={this.state.modulesBuildHistory} />
      </PageContainer>
    );
  }
}

export default DashboardContainer;
