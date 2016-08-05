import React, {PropTypes} from 'react';

const Sidebar = ({}) => {
  let headline;

  if (this.props.headline) {
    headline = <h2 className="sidebar__headline">{this.props.headline}</h2>;
  }

  return (
    <div className="sidebar">
      {headline}
      {this.props.children}
    </div>
  );
};

Sidebar.propTypes = {
  headline: PropTypes.string,
  children: PropTypes.node
};

export default Sidebar;
