import React, {Component, PropTypes} from 'react';
import {bindAll} from 'underscore';

class PageHeader extends Component {

  constructor(props) {
    super(props);
  }

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
