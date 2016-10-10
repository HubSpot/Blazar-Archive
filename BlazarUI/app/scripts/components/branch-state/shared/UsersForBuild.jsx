import React, { PropTypes } from 'react';
import BuildTriggerTypes from '../../../constants/BuildTriggerTypes';

import Icon from '../../shared/Icon.jsx';

const getUsers = (buildTrigger, commitInfo) => {
  switch (buildTrigger.get('type')) {
    case BuildTriggerTypes.PUSH:
    case BuildTriggerTypes.BRANCH_CREATION: {
      const newCommitAuthors = commitInfo.get('newCommits')
        .map((commit) => commit.getIn(['author', 'username']) || commit.getIn(['author', 'email']))
        .toOrderedSet()
        .join(', ');

      // For newly created branches (currently type PUSH), there are no new commit authors
      return newCommitAuthors || commitInfo.getIn(['current', 'author', 'username']);
    }

    case BuildTriggerTypes.MANUAL: {
      const user = buildTrigger.get('id');
      return user === 'unknown' ? 'unknown user' : user;
    }

    default:
      return '';
  }
};

const UsersForBuild = ({branchBuild}) => {
  const buildTrigger = branchBuild.get('buildTrigger');
  const commitInfo = branchBuild.get('commitInfo');
  const relevantUsers = getUsers(buildTrigger, commitInfo);
  if (!relevantUsers) {
    return null;
  }

  return <span className="users-for-build"><Icon name="user" /> {relevantUsers}</span>;
};

UsersForBuild.propTypes = {
  branchBuild: PropTypes.object.isRequired
};

export default UsersForBuild;
