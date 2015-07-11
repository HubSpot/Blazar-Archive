import React from 'react';
import Dashboard from './Dashboard.jsx';
import PageContainer from '../layout/PageContainer.jsx'

class DashboardContainer extends React.Component {

  constructor(props){
    super(props);
  }

  render() {
    return (
      <PageContainer classNames='page-dashboard'>
        <Dashboard />
      </PageContainer>
    );
  }
}

export default DashboardContainer;