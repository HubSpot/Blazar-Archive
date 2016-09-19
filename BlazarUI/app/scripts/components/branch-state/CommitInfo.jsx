import React from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import Icon from '../shared/Icon.jsx';
import CommitLink from './CommitLink.jsx';
import CompareCommitsLink from './CompareCommitsLink.jsx';

const CommitInfo = ({commitInfo}) => {
  const previousCommit = <CommitLink commit={commitInfo.get('previous')} />;
  const currentCommit = <CommitLink commit={commitInfo.get('current')} />;
  const arrow = <Icon name="long-arrow-right" />;
  const compareLink = <CompareCommitsLink className="commit-info__compare-link" commitInfo={commitInfo} />;

  return (
    <p className="module-build__commit-info">
      {previousCommit} {arrow} {currentCommit}{compareLink}
    </p>
  );
};

CommitInfo.propTypes = {
  commitInfo: ImmutablePropTypes.mapContains({
    current: ImmutablePropTypes.map,
    previous: ImmutablePropTypes.map
  })
};

export default CommitInfo;
