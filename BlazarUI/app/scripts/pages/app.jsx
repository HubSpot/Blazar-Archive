import React from 'react';
import { RouteHandler } from 'react-router';
import Navigation from '../components/navigation/navigation.jsx'

class App extends React.Component {
  
  render() {
    return (
      <div>
        <Navigation />
        <div className="page-wrapper">
          <RouteHandler/>
        </div>
      </div>
    );
  }
  
}

export default App;