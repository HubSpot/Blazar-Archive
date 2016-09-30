import React, {PropTypes} from 'react';

const PageHeader = ({children}) => (
  <header className="page-header">
    {children}
  </header>
);

PageHeader.propTypes = {
  children: PropTypes.node
};

const PageTitle = ({children}) => (
  <h1 className="page-title">
    {children}
  </h1>
);

PageTitle.propTypes = {
  children: PropTypes.node
};

PageHeader.PageTitle = PageTitle;

export default PageHeader;
