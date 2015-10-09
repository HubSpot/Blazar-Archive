import React, {Component} from 'react';
import DashboardContainer from '../components/dashboard/DashboardContainer.jsx';
import PageHeaderContainer from '../components/PageHeader/PageHeaderContainer.jsx';

class Home extends Component {

  render() {
    return (
      <div>
        <PageHeaderContainer 
          params={this.props.params}
        />
        <DashboardContainer 
          params={this.props.params}
        />
      </div>
    );
  }
}

export default Home;
