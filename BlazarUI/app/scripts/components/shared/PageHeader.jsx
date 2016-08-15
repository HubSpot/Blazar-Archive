import React, {PropTypes} from 'react';

const PageHeader = ({children}) => (
  <div className="page-header">
    {children}
  </div>
);

PageHeader.propTypes = {
  children: PropTypes.node
};

export default PageHeader;
