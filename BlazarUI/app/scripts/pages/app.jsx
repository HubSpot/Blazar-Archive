import React from 'react';
import { RouteHandler } from 'react-router';
import Navigation from '../components/navigation/navigation.jsx'
import ProjectsSidebarContainer from '../components/sidebar/projects/ProjectsSidebarContainer.jsx'

class App extends React.Component {

  render() {
    return (
      <div>
        <Navigation />
        <div className="page-wrapper">
          <ProjectsSidebarContainer/>
          <RouteHandler/>
        </div>
      </div>
    );
  }

}

export default App;