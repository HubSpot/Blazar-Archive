import React, { PropTypes } from 'react';
import { OrderedSet } from 'immutable';
import Tooltip from 'react-bootstrap/lib/Tooltip';
import OverlayTrigger from 'react-bootstrap/lib/OverlayTrigger';

import Icon from '../../shared/Icon.jsx';
import BuildTriggerTypes from '../../../constants/BuildTriggerTypes';

// prioritize author information the same way that GitHub does
// show GitHub username if available, else use the Git name or email
const getAuthor = (commit) =>
  commit.getIn(['author', 'username']) ||
  commit.getIn(['author', 'name']) ||
  commit.getIn(['author', 'email']);

const getUsers = (buildTrigger, commitInfo) => {
  switch (buildTrigger.get('type')) {
    case BuildTriggerTypes.PUSH:
    case BuildTriggerTypes.BRANCH_CREATION: {
      const newCommitAuthors = commitInfo.get('newCommits')
        .map(getAuthor)
        .toOrderedSet();

      // For newly created branches (currently type PUSH), there are no new commit authors
      if (newCommitAuthors.isEmpty() && commitInfo.has('current')) {
        const currentCommit = commitInfo.get('current');
        return OrderedSet.of(getAuthor(currentCommit));
      }

      return newCommitAuthors;
    }

    case BuildTriggerTypes.MANUAL: {
      const user = buildTrigger.get('id');
      const isUserUnknown = !user || user === 'unknown';
      return OrderedSet.of(isUserUnknown ? 'unknown user' : user);
    }

    default:
      return new OrderedSet();
  }
};

const UsersForBuild = ({branchBuild}) => {
  const buildTrigger = branchBuild.get('buildTrigger');
  const commitInfo = branchBuild.get('commitInfo');
  const relevantUsers = getUsers(buildTrigger, commitInfo);
  if (relevantUsers.isEmpty()) {
    return null;
  }

  let additionalUsers = null;
  if (relevantUsers.size > 1) {
    const branchBuildId = branchBuild.get('id');
    const tooltip = (
      <Tooltip id={`additional-users-build-${branchBuildId}`}>
        <ul className="list-unstyled users-for-build__additional-users-list">
          {relevantUsers.skip(1).map((user) => <li key={user}>{user}</li>)}
        </ul>
      </Tooltip>
    );
    const numberOfAdditionalUsers = relevantUsers.size - 1;
    additionalUsers = (
      <OverlayTrigger placement="bottom" overlay={tooltip}>
        <span className="users-for-build__additional-users">
          +{numberOfAdditionalUsers} other{numberOfAdditionalUsers > 1 && 's'}
        </span>
      </OverlayTrigger>
    );
  }

  return (
    <span className="users-for-build">
      <Icon name="user" /> {relevantUsers.first()} {additionalUsers}
    </span>
  );
};

UsersForBuild.propTypes = {
  branchBuild: PropTypes.object.isRequired
};

export default UsersForBuild;
