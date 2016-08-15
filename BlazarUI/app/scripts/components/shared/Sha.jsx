import React, {PropTypes} from 'react';
import {truncate} from '../Helpers';

const Sha = ({build, truncateMaxLength}) => {
  if (!build.commitInfo) {
    return null; // TODO: find this info somewhere else, then
  }

  const commitLink = build.commitInfo.current.url;

  return (
    <span className="sha">
      <a href={commitLink} className="sha-link" target="_blank">{truncate(build.sha, truncateMaxLength)}</a>
    </span>
  );
};

Sha.defaultProps = {
  truncateMaxLength: 10
};

Sha.propTypes = {
  build: PropTypes.object.isRequired,
  truncateMaxLength: PropTypes.number
};

export default Sha;
