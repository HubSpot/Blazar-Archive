import React, {Component, PropTypes} from 'react';
import { Link } from 'react-router';
import Image from '../shared/Image.jsx';

class SidebarLogo extends Component {

  renderImage() {
    if (this.props.mini) {
      return <Image classNames="sidebar__logo-image" src={`${window.config.staticRoot}/images/icon.png`} width={35} height={35} />;
    } else {
      return <Image classNames="sidebar__logo-image" src={`${window.config.staticRoot}/images/blazar-logo.png`} />;
    }
  }

  render() {
    return (
      <div className="sidebar__logo">
        <Link to="dashboard">
          {this.renderImage()}
        </Link>
      </div>
    );
  }
}

SidebarLogo.propTypes = {
  mini: PropTypes.bool
};

export default SidebarLogo;
