import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';

import BuildTriggerLabel from './shared/BuildTriggerLabel.jsx';
import UsersForBuild from './shared/UsersForBuild.jsx';
import CommitInfo from './shared/CommitInfo.jsx';
import CancelBuildButton from '../shared/branch-build/CancelBuildButton.jsx';
import CommitsSummary from './shared/CommitsSummary.jsx';

const BranchBuildHeader = ({branchBuild, onCancelBuild}) => {
  const buildNumber = branchBuild.get('buildNumber');
  const buildTrigger = branchBuild.get('buildTrigger');
  const commitInfo = branchBuild.get('commitInfo');
  const buildId = branchBuild.get('id');

  return (
    <div className="branch-build-header">
      <h3 className="branch-build-header__build-number">
        Build #{buildNumber}
      </h3>
      <span className="branch-build-header__build-trigger-label-wrapper">
        <BuildTriggerLabel buildTrigger={buildTrigger} />
      </span>
      <UsersForBuild branchBuild={branchBuild} />
      <CommitsSummary className="branch-build-header__commits" commitInfo={commitInfo} buildId={buildId} />
      <span className="branch-build-header__action-items">
        <CancelBuildButton
          onCancel={onCancelBuild}
          build={branchBuild.toJS()}
          btnStyle="link"
          btnClassName="branch-build-header__cancel-build-button"
        />
        <CommitInfo commitInfo={commitInfo} />
      </span>
    </div>
  );
};

BranchBuildHeader.propTypes = {
  branchBuild: ImmutablePropTypes.mapContains({
    buildNumber: PropTypes.number.isRequired,
    buildTrigger: ImmutablePropTypes.map.isRequired,
    commitInfo: ImmutablePropTypes.map.isRequired
  }),
  onCancelBuild: PropTypes.func.isRequired
};

export default BranchBuildHeader;
