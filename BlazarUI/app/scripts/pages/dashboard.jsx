import React from 'react';
import UIGrid from '../components/shared/grid/UIGrid.jsx'
import UIGridItem from '../components/shared/grid/UIGridItem.jsx'
import DashboardContainer from '../components/dashboard/DashboardContainer.jsx'

class Home extends React.Component {

  constructor(props){
    super(props);
  }

  render() {
    return (
      <div>
          <DashboardContainer />
      </div>
    );
  }
}

export default Home;



