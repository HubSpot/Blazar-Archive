import React, {Component, PropTypes} from 'react';

class Sidebar extends Component {

  render() {
    let headline;
    if (this.props.headline) {
      headline = <h2 className='sidebar__headline'>{this.props.headline}</h2>;
    }
    return (
      <div className='sidebar'>
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
  children: PropTypes.node
};

export default Sidebar;
