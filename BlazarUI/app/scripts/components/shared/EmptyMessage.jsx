import React, { Component, PropTypes } from 'react';

class EmptyMessage extends Component {
  
  render() {
    return (
      <div className='empty-message'>
        {this.props.children}
      </div>
    )
  }

};

EmptyMessage.propTypes = {
  children: PropTypes.node
};

export default EmptyMessage;
