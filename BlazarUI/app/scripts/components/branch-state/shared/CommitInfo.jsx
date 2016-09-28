import React from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import Icon from '../../shared/Icon.jsx';
import CommitLink from './CommitLink.jsx';
import CompareCommitsLink from './CompareCommitsLink.jsx';

const CommitInfo = ({commitInfo}) => {
  const previousCommit = commitInfo.get('previous');
  const currentCommit = commitInfo.get('current');
  if (!previousCommit || previousCommit.get('id') === currentCommit.get('id')) {
    return (
      <div className="module-build__commit-info" onClick={(e) => e.stopPropagation()}>
        <CommitLink commit={commitInfo.get('current')} />
      </div>
    );
  }

  const previousCommitLink = <CommitLink commit={previousCommit} />;
  const currentCommitLink = <CommitLink commit={currentCommit} />;
  const arrow = <Icon name="long-arrow-right" />;
  const compareLink = <CompareCommitsLink className="commit-info__compare-link" commitInfo={commitInfo} />;

  return (
    <div className="module-build__commit-info" onClick={(e) => e.stopPropagation()}>
      {previousCommitLink} {arrow} {currentCommitLink}{compareLink}
    </div>
  );
};

CommitInfo.propTypes = {
  commitInfo: ImmutablePropTypes.mapContains({
    current: ImmutablePropTypes.map,
    previous: ImmutablePropTypes.map
  })
};

export default CommitInfo;
