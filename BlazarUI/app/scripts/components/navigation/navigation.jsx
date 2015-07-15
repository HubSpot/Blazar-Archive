import React from 'react';
import { Link } from 'react-router';

class Navigation extends React.Component {

  render() {
    return (
        <nav className="navbar navbar-default navbar-dark" role="navigation">
          <div className="container-fluid">
            <div className="navbar-header">
              <Link className="navbar-brand" to="dashboard">
                Blazar
              </Link>
            </div>
          </div>
        </nav>
    );
  }

}

Navigation.contextTypes = {
  router: React.PropTypes.func.isRequired
};

export default Navigation;
