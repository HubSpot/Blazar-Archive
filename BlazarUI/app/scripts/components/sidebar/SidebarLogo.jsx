import React, {Component, PropTypes} from 'react';
import { Link } from 'react-router';
import Image from '../shared/Image.jsx';

class SidebarLogo extends Component {

  render() {
    const imgPath = `${window.config.staticRoot}/images/blazar-logo.png`;
    return (
      <div className="sidebar__logo">
        <Link to="dashboard">
          <Image classNames="sidebar__logo-image" src={imgPath} />
        </Link>
      </div>
    );
  }

}

export default SidebarLogo;
