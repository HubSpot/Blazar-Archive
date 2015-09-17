import React, {Component, PropTypes} from 'react';
import Button from 'react-bootstrap/lib/Button';
import Icon from '../shared/Icon.jsx';

class Sidebar extends Component {

  renderCollapseButton() {
    return (
      <Button className="sidebar__collapse-button" onClick={this.props.collapse}>
        <Icon for={this.props.isCollapsed ? 'right' : 'left'}></Icon>
      </Button>
    );
  }

  renderHeadline() {
    if (this.props.headline) {
      return <h2 className='sidebar__headline'>{this.props.headline}</h2>;
    }
  }

  getSidebarClasses() {
    return this.props.isCollapsed ? 'sidebar-collapsed' : '';
  }

  render() {
    return (
      <div className={`sidebar ${this.getSidebarClasses()}`}>
        {this.renderCollapseButton()}
        <div className='sidebar-inner'>
          {this.renderHeadline()}
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
