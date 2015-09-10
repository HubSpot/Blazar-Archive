import React, {Component, PropTypes} from 'react';

class HeadlineDetail extends Component{

  render() {
    return (
      <span className='headline__detail'>
        {' '} <span className='headline__detail-subheadline'>{this.props.children}</span>
      </span>
    );
  }
}

HeadlineDetail.propTypes = {
  children: PropTypes.node
};

export default HeadlineDetail;
