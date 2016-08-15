import React, {PropTypes} from 'react';

const Sidebar = ({headline, children}) => (
  <div className="sidebar">
    {headline && <h2 className="sidebar__headline">{headline}</h2>}
    {children}
  </div>
);

Sidebar.propTypes = {
  headline: PropTypes.string,
  children: PropTypes.node
};

export default Sidebar;
