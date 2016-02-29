import React, {Component, PropTypes} from 'react';
import { Link } from 'react-router';
import Image from '../shared/Image.jsx';

class Navigation extends Component {

  render() {
    const imgPath = `${window.config.staticRoot}/images/blazar-logo.ng`;

    return (
        <nav id='primary-nav' className="navbar navbar-default navbar-dark" role="navigation">
          <div className="container-fluid">
            <div className="navbar-header">
              <Link className="navbar-brand" to="dashboard">
                <Image classNames="title-image" src={imgPath} />
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
