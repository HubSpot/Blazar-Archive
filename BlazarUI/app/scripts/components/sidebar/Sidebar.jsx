import React from 'react';

class Sidebar extends React.Component {

  render() {
    let headline;
    if (this.props.headline) {
      headline = <h2 className='sidebar__headline'>{this.props.headline}</h2>;
    }
    return (
      <div className='sidebar'>
        {headline}
        {this.props.children}
      </div>
    );
  }
}

Sidebar.propTypes = {
  headline: React.PropTypes.string,
  children: React.PropTypes.node
};

export default Sidebar;
