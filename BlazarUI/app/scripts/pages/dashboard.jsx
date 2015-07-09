import React from 'react';
import UIGrid from '../components/shared/grid/UIGrid.jsx'
import UIGridItem from '../components/shared/grid/UIGridItem.jsx'

import RequestSidebarContainer from '../components/sidebar/RequestSidebarContainer.jsx'
import Dashboard from '../components/dashboard/Dashboard.jsx'

class Home extends React.Component {
  
  constructor(props){
    super(props);
  }

  render() {
    return (
      <UIGrid>
        
        <UIGridItem size={3}>
          <RequestSidebarContainer/>
        </UIGridItem>

        <UIGridItem size={8}>
          <Dashboard />
        </UIGridItem>

      </UIGrid>
    );
  }
}

export default Home;



