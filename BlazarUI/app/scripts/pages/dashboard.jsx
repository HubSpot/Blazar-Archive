import React, {Component} from 'react';
import DashboardContainer from '../components/dashboard/DashboardContainer.jsx';
import HeaderContainer from '../components/header/HeaderContainer.jsx';

class Home extends Component {

  render() {
    return (
      <div>
        <HeaderContainer 
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
