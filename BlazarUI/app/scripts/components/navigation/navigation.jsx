import React from 'react';
import { Link } from 'react-router';

class Navigation extends React.Component{

  constructor(props, context) {
   super(props);
  }

  render() {
    return (  
        <nav className="navbar navbar-default" role="navigation">
          <div className="container-fluid">
            <div className="navbar-header">
              <a className="navbar-brand" href="/">
              Blazar
              </a>
            </div>
          </div>
        </nav>
    );
  }

}


Navigation.contextTypes = {
  router: React.PropTypes.func.isRequired
}

export default Navigation;