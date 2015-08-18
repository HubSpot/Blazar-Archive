import React, {Component, PropTypes} from 'react';

class PageHeader extends Component{

  render() {
    return (
      <div className='page-header'>
        {this.props.children}
      </div>
    );
  }
}

PageHeader.propTypes = {
  children: PropTypes.node
};

export default PageHeader;
