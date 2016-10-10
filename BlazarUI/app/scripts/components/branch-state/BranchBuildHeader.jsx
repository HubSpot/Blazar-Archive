import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';

import BuildTriggerLabel from './shared/BuildTriggerLabel.jsx';
import UsersForBuild from './shared/UsersForBuild.jsx';
import CommitInfo from './shared/CommitInfo.jsx';

const ModuleList = ({branchBuild}) => {
  const buildNumber = branchBuild.get('buildNumber');
  const buildTrigger = branchBuild.get('buildTrigger');
  const commitInfo = branchBuild.get('commitInfo');

  return (
    <div className="branch-build-header">
      <h3 className="branch-build-header__build-number">
        Build #{buildNumber}
      </h3>
      <span className="branch-build-header__build-trigger-label-wrapper">
        <BuildTriggerLabel buildTrigger={buildTrigger} />
      </span>
      <UsersForBuild branchBuild={branchBuild} />
      <span className="branch-build-header__commit-info-wrapper">
        <CommitInfo commitInfo={commitInfo} />
      </span>
    </div>
  );
};

ModuleList.propTypes = {
  branchBuild: ImmutablePropTypes.mapContains({
    buildNumber: PropTypes.number.isRequired,
    buildTrigger: ImmutablePropTypes.map.isRequired,
    commitInfo: ImmutablePropTypes.map.isRequired
  })
};

export default ModuleList;
