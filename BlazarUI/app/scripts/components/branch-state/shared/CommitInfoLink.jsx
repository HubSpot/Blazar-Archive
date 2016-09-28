import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import Icon from '../../shared/Icon.jsx';
import CompareCommitsLink from '../shared/CompareCommitsLink.jsx';

const CommitInfoLink = ({commitInfo, className}) => {
  const previousCommit = commitInfo.get('previous');
  const currentCommit = commitInfo.get('current');
  if (!previousCommit || previousCommit.get('id') === currentCommit.get('id')) {
    const url = currentCommit.get('url');
    return (
      <a className={className} href={url} target="_blank" onClick={(e) => e.stopPropagation()}>
        <Icon type="octicon" name="mark-github" />
      </a>
    );
  }

  return <CompareCommitsLink commitInfo={commitInfo} className={className} />;
};

CommitInfoLink.propTypes = {
  commitInfo: ImmutablePropTypes.mapContains({
    current: ImmutablePropTypes.map,
    previous: ImmutablePropTypes.map
  }),
  className: PropTypes.string
};

export default CommitInfoLink;
