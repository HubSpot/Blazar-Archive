import React, {Component} from 'react';
import Dashboard from './Dashboard.jsx';
import PageContainer from '../shared/PageContainer.jsx';
import BuildsStore from '../../stores/buildsStore';
import BuildsActions from '../../actions/buildsActions';

class DashboardContainer extends Component {

  constructor(props) {
    super(props);

    this.state = {
      loading: false
    };
  }

  componentDidMount() {
    this.unsubscribe = BuildsStore.listen(this.onStatusChange.bind(this));
    BuildsActions.loadBuilds();
  }

  componentWillUnmount() {
    this.unsubscribe();
  }

  onStatusChange(state) {
    this.setState(state);
  }

  render() {
    return (
      <PageContainer classNames='page-dashboard'>
        <Dashboard builds={this.state.builds} loading={this.state.loading} />
      </PageContainer>
    );
  }
}

export default DashboardContainer;
