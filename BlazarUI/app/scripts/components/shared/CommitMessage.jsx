import React, {Component, PropTypes} from 'react';
import {truncate as Truncate} from '../Helpers';

class CommitMessage extends Component {

  render() {
    
    const {
      message,
      truncate,
      truncateLen,
      truncateEllipsis
    } = this.props;
    
    return (
      <span className='pre-text' title={this.props.message}>
        {truncate ? Truncate(message, truncateLen, truncateEllipsis) : message}
      </span>
    );
  }

}

CommitMessage.defaultProps = {
  truncate: true,
  truncateLen: 40,
  truncateEllipsis: true
};

CommitMessage.propTypes = {
  message: PropTypes.string.isRequired,
  truncate: PropTypes.bool,
  truncateLen: PropTypes.number,
  truncateEllipsis: PropTypes.bool
};

export default CommitMessage;
