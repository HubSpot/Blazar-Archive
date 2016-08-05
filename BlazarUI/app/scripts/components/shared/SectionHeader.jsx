import React, {PropTypes} from 'react';

const SectionHeader = ({children}) => (
  <h3 className="section-header">
    {children}
  </h3>
);

SectionHeader.propTypes = {
  children: PropTypes.node
};

export default SectionHeader;
