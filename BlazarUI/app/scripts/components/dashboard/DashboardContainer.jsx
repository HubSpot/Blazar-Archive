import React, {Component} from 'react';
import Dashboard from './Dashboard.jsx';
import PageContainer from '../shared/PageContainer.jsx';
import BuildsStore from '../../stores/buildsStore';
import BuildsActions from '../../actions/buildsActions';

import StarActions from '../../actions/starActions';
import StarStore from '../../stores/starStore';


class DashboardContainer extends Component {


  constructor(props) {
    super(props);

    this.state = {
      builds: {},
      stars: [], 
      loadingBuilds: true,
      loadingStars: true,
      loading: true
    };
  }

  componentDidMount() {
    this.unsubscribeFromBuilds = BuildsStore.listen(this.onStatusChange.bind(this));
    this.unsubscribeFromStars = StarStore.listen(this.onStatusChange.bind(this));

    BuildsActions.loadBuilds();
    StarActions.loadStars();
  }

  componentWillUnmount() {
    this.unsubscribeFromBuilds();
    this.unsubscribeFromStars();
  }

  onStatusChange(state) {
    this.setState(state);

    if (!this.state.loadingBuilds && !this.state.loadingStars) {
      this.setState({
        loading: false
      })
    }

  }



  render() {
    return (
      <PageContainer classNames='page-dashboard'>
        <Dashboard 
          builds={this.state.builds} 
          stars={this.state.stars}
          loading={this.state.loading} />
      </PageContainer>
    );
  }
}

export default DashboardContainer;
