import React, {Component} from 'react';
import DashboardContainer from '../components/dashboard/DashboardContainer.jsx';

class Home extends Component {

  render() {
    return (
        <DashboardContainer 
          params={this.props.params}
        />
    );
  }
}

export default Home;
