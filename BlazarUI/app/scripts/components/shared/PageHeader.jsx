import React from 'react';

class PageHeader extends React.Component{

  render() {
    return (
      <div className='page-header'>
        {this.props.children}
      </div>
    );
  }
}

PageHeader.propTypes = {
  children: React.PropTypes.node
};

export default PageHeader;
