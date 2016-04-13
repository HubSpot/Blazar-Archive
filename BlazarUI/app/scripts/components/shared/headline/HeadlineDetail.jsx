import React, {Component, PropTypes} from 'react';

class HeadlineDetail extends Component{

  render() {

  	let extraClass = 'headline__detail' + (this.props.block ? ' headline-block' : '');
    extraClass += this.props.crumb ? ' headline-crumb' : '';

    return (
      <span className={extraClass}>
        {' '} <span className='headline__detail-subheadline'>{this.props.children}</span>
      </span>
    );
  }
}

HeadlineDetail.propTypes = {
  children: PropTypes.node,
  block: PropTypes.bool,
  crumb: PropTypes.bool
};

export default HeadlineDetail;
