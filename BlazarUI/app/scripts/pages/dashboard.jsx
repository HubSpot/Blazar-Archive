import React from 'react';
import UIGrid from '../components/shared/grid/UIGrid.jsx'
import UIGridItem from '../components/shared/grid/UIGridItem.jsx'

import ProjectsSidebarContainer from '../components/sidebar/projects/ProjectsSidebarContainer.jsx'
import Dashboard from '../components/dashboard/Dashboard.jsx'

class Home extends React.Component {

  constructor(props){
    super(props);
  }

  render() {
    return (
      <UIGrid>

        <UIGridItem size={3}>
          <ProjectsSidebarContainer/>
        </UIGridItem>

        <UIGridItem size={8}>
          <Dashboard />
        </UIGridItem>

      </UIGrid>
    );
  }
}

export default Home;



