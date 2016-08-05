import React, {PropTypes} from 'react';
import {truncate as Truncate} from '../Helpers';

const CommitMessage = ({message, truncate, truncateLen, truncateEllipsis}) => (
  <span className="pre-text" title={message}>
    {truncate ? Truncate(message, truncateLen, truncateEllipsis) : message}
  </span>
);

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
