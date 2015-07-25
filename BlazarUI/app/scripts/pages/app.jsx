import React, {Component} from 'react';
import {RouteHandler} from 'react-router';
import Navigation from '../components/navigation/navigation.jsx';
import BuildsSidebarContainer from '../components/sidebar/BuildsSidebarContainer.jsx';

class App extends Component{

  render() {
    return (
      <div>
        <Navigation />
        <div className="page-wrapper">
          <BuildsSidebarContainer/>
          <RouteHandler/>
        </div>
      </div>
    );
  }

}

export default App;
