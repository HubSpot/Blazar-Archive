import React from 'react';
import UIGrid from '../components/shared/grid/UIGrid.jsx'
import UIGridItem from '../components/shared/grid/UIGridItem.jsx'


import Sidebar from '../components/sidebar/Sidebar.jsx'
import Dashboard from '../components/dashboard/Dashboard.jsx'

class Home extends React.Component {
  
  constructor(props){
    super(props);
  }

  render() {
    return (
      <UIGrid>
        
        <UIGridItem size={3}>
          <Sidebar />
        </UIGridItem>

        <UIGridItem size={8}>
          <Dashboard />
        </UIGridItem>

      </UIGrid>
    );
  }
}

export default Home;



