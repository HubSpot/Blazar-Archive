import React, {Component, PropTypes} from 'react';

class HeadlineDetail extends Component{

  render() {
    return (
      <span className='headline__detail'>
        {' '} · {this.props.children}
      </span>
    );
  }
}

HeadlineDetail.propTypes = {
  children: PropTypes.node
};

export default HeadlineDetail;
