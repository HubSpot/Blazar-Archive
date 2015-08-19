import React, {Component, PropTypes} from 'react';
import { Link } from 'react-router';

class Navigation extends Component {

  render() {
    let imgPath = `${config.appRoot}/images/title.png`;
    return (
        <nav className="navbar navbar-default navbar-dark" role="navigation">
          <div className="container-fluid">
            <div className="navbar-header">
              <Link className="navbar-brand" to="dashboard">
                <img className="title-image" src={imgPath} />
              </Link>
            </div>
          </div>
        </nav>
    );
  }

}

Navigation.contextTypes = {
  router: PropTypes.func.isRequired
};

export default Navigation;
