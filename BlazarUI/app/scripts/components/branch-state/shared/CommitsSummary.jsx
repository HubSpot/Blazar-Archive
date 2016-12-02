import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import Immutable from 'immutable';

import Button from 'react-bootstrap/lib/Button';
import OverlayTrigger from 'react-bootstrap/lib/OverlayTrigger';
import Popover from 'react-bootstrap/lib/Popover';

import Icon from '../../shared/Icon.jsx';
import CommitsSummaryPopover from './CommitsSummaryPopover.jsx';
import { getCompareUrl } from '../../../utils/commitInfoHelpers';

const CommitsSummary = ({commitInfo, popoverPlacement, buildId}) => {
  const currentCommit = commitInfo.get('current');
  const newCommits = commitInfo.get('newCommits');

  let text;
  let commitList;
  let viewCommitsInGitHubUrl;

  const hasNewCommits = !newCommits.isEmpty();

  if (hasNewCommits) {
    commitList = newCommits.sort((a, b) => b.get('timestamp') - a.get('timestamp'));
    text = (commitList.size === 1) ? '1 commit' : `${commitList.size} commits`;
    viewCommitsInGitHubUrl = getCompareUrl(commitInfo);
  } else {
    commitList = Immutable.List.of(currentCommit);
    text = 'latest commit';
    viewCommitsInGitHubUrl = currentCommit.get('url');
  }

  const popover = (
    <Popover id={`commit-summary-popover-${buildId}`}>
      <CommitsSummaryPopover
        commitList={commitList}
        viewCommitsInGitHubUrl={viewCommitsInGitHubUrl}
      />
    </Popover>
  );

  return (
    <span>
      <OverlayTrigger trigger="click" rootClose={true} placement={popoverPlacement} overlay={popover}>
        <Button bsStyle="link" className="commits-summary">
          <Icon type="octicon" name="git-commit" classNames="commits-summary-icon" />{text}
        </Button>
      </OverlayTrigger>
    </span>
  );
};

CommitsSummary.propTypes = {
  commitInfo: ImmutablePropTypes.mapContains({
    current: ImmutablePropTypes.map,
    newCommits: ImmutablePropTypes.list
  }),
  popoverPlacement: PropTypes.string,
  buildId: PropTypes.number.isRequired
};

CommitsSummary.defaultProps = {
  popoverPlacement: 'bottom'
};

export default CommitsSummary;
