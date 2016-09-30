import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';

const CommitLink = ({commit}) => {
  const truncatedSha = commit.get('id').substr(0, 8);
  return (
    <a className="commit-link" href={commit.get('url')} target="_blank">
      {truncatedSha}
    </a>
  );
};

CommitLink.propTypes = {
  commit: ImmutablePropTypes.mapContains({
    url: PropTypes.string.isRequired,
    id: PropTypes.string.isRequired
  })
};

export default CommitLink;
