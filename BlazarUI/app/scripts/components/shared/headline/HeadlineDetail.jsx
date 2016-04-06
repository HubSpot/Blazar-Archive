import React, {Component, PropTypes} from 'react';

class HeadlineDetail extends Component{

  render() {

  	let extraClass = 'headline__detail' + (this.props.block ? ' headline-block' : '');

    return (
      <span className={extraClass}>
        {' '} <span className='headline__detail-subheadline'>{this.props.children}</span>
      </span>
    );
  }
}

HeadlineDetail.propTypes = {
  children: PropTypes.node,
  block: PropTypes.bool
};

export default HeadlineDetail;
