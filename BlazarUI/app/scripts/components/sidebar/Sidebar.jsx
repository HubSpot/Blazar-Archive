import React, {Component, PropTypes} from 'react';
import Button from 'react-bootstrap/lib/Button';
import Icon from '../shared/Icon.jsx';

class Sidebar extends Component {

  render() {
    const collapseButton = (
      <Button className="sidebar__collapse-button" onClick={this.props.collapse}>
        <Icon for={this.props.isCollapsed ? 'right' : 'left'}></Icon>
      </Button>
    );

    const sidebarClasses = this.props.isCollapsed ? 'sidebar-collapsed' : '';

    let headline;
    if (this.props.headline) {
      headline = <h2 className='sidebar__headline'>{this.props.headline}</h2>;
    }
    return (
      <div className={`sidebar ${sidebarClasses}`}>
        {collapseButton}
        <div className='sidebar-inner'>
          {headline}
          {this.props.children}
        </div>
      </div>
    );
  }
}

Sidebar.propTypes = {
  headline: PropTypes.string,
  children: PropTypes.node,
  collapse: PropTypes.func,
  isCollapsed: PropTypes.bool
};

export default Sidebar;
