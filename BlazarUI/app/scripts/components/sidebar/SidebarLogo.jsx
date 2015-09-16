import React, {Component, PropTypes} from 'react';
import { Link } from 'react-router';
import Image from '../shared/Image.jsx';

class SidebarLogo extends Component {

  render() {
    let imgPath;
    let image;
    if (this.props.mini) {
      imgPath = `${window.config.staticRoot}/images/icon.png`;
      image = <Image classNames="sidebar__logo-image" src={imgPath} width={35} height={35} />;
    } else {
      imgPath = `${window.config.staticRoot}/images/blazar-logo.png`;
      image = <Image classNames="sidebar__logo-image" src={imgPath} />;
    }

    return (
      <div className="sidebar__logo">
        <Link to="dashboard">
        {image}
        </Link>
      </div>
    );
  }
}

SidebarLogo.propTypes = {
  mini: PropTypes.bool
};

export default SidebarLogo;
