import React, {Component} from 'react';
import Dashboard from './Dashboard.jsx';
import PageContainer from '../shared/PageContainer.jsx';

class DashboardContainer extends Component {

  render() {
    return (
      <PageContainer classNames='page-dashboard'>
        <Dashboard />
      </PageContainer>
    );
  }
}

export default DashboardContainer;
