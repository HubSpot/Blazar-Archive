import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import classNames from 'classnames';

import Icon from '../../shared/Icon.jsx';
import { truncate } from '../../Helpers';

const MAX_COMMITS_DISPLAYED = 5;
const MAX_DISPLAYED_COMMIT_MESSAGE_LENGTH = 80;

const CommitRowIcon = ({isFirstRow}) => {
  if (isFirstRow) {
    return (
      <div className="commits-summary-poverover__branch-head-icon">
        <Icon type="octicon" name="git-branch" />
      </div>
    );
  }

  return (
    <Icon
      type="octicon"
      name="git-commit"
      classNames="commits-summary-poverover__git-commit-icon"
    />
  );
};

CommitRowIcon.propTypes = {
  isFirstRow: PropTypes.bool.isRequired
};

const CommitRow = ({commit, index}) => {
  const isFirstRow = index === 0;
  const commitIconClassName = classNames('commits-summary-poverover__commit-icon', {
    'commits-summary-poverover__commit-icon--first': isFirstRow
  });

  const commitMessage = commit.get('message');
  const formattedCommitMessage = truncate(commitMessage, MAX_DISPLAYED_COMMIT_MESSAGE_LENGTH, true);

  return (
    <tr className="commits-summary-poverover__commit">
      <td className={commitIconClassName}>
        <CommitRowIcon isFirstRow={isFirstRow} />
      </td>
      <td className="commits-summary-popover__commit-message">
        {formattedCommitMessage}
      </td>
    </tr>
  );
};

CommitRow.propTypes = {
  commit: ImmutablePropTypes.mapContains({
    message: PropTypes.string.isRequired,
    url: PropTypes.string.isRequired
  }),
  index: PropTypes.number.isRequired
};

const CommitsSummaryPopover = ({commitList, viewCommitsInGitHubUrl}) => {
  const truncateCommits = commitList.size > MAX_COMMITS_DISPLAYED;
  const displayedCommits = commitList.take(MAX_COMMITS_DISPLAYED);
  const singleCommit = commitList.size === 1;

  let externalLinkText;
  if (singleCommit) {
    externalLinkText = 'View in GitHub';
  } else if (truncateCommits) {
    externalLinkText = `View all ${commitList.size} commits in GitHub`;
  } else {
    externalLinkText = 'View commits in GitHub';
  }

  const tableClassNames = classNames('commits-summary-popover__commit-list',
    {'commits-summary-popover__commit-list--single-commit': singleCommit});

  return (
    <div className="commits-summary-popover">
      <table className={tableClassNames}>
        <tbody>
          {displayedCommits.map((commit, index) =>
            <CommitRow key={commit.get('id')} commit={commit} index={index} />)
          }
        </tbody>
      </table>
      <p className="commits-summary-popover__footer">
        <a
          className="commits-summary-popover__view-commits-link"
          href={viewCommitsInGitHubUrl}
          target="_blank"
        >
          {externalLinkText} <Icon name="external-link" />
        </a>
      </p>
    </div>
  );
};

CommitsSummaryPopover.propTypes = {
  commitList: ImmutablePropTypes.list.isRequired,
  viewCommitsInGitHubUrl: PropTypes.string.isRequired
};

export default CommitsSummaryPopover;
