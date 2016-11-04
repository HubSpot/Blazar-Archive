import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';

import BuildTriggerLabel from './shared/BuildTriggerLabel.jsx';
import UsersForBuild from './shared/UsersForBuild.jsx';
import CancelBuildButton from '../shared/branch-build/CancelBuildButton.jsx';
import CommitsSummary from './shared/CommitsSummary.jsx';
import BranchBuildProgress from './shared/BranchBuildProgress.jsx';
import Icon from '../shared/Icon.jsx';

import { buildIsInactive } from '../Helpers';

const BranchBuildHeader = ({branchBuild, completedModuleBuildCount, totalNonSkippedModuleBuildCount, onCancelBuild}) => {
  const buildNumber = branchBuild.get('buildNumber');
  const buildTrigger = branchBuild.get('buildTrigger');
  const commitInfo = branchBuild.get('commitInfo');
  const buildId = branchBuild.get('id');
  const branchBuildState = branchBuild.get('state');
  const isActiveBuild = !buildIsInactive(branchBuildState);

  return (
    <div className="branch-build-header">
      <div className="branch-build-header__build-number">
        {isActiveBuild && <Icon for="spinner" classNames="branch-build-header__active-build-icon" />} #{buildNumber}
      </div>
      <div className="branch-build-header__build-summary">
        <BranchBuildProgress
          completedModuleBuildCount={completedModuleBuildCount}
          totalNonSkippedModuleBuildCount={totalNonSkippedModuleBuildCount}
          branchBuildState={branchBuildState}
        />
        <div className="branch-build-header__build-summary-info">
          <span className="branch-build-header__build-trigger-label-wrapper">
            <BuildTriggerLabel buildTrigger={buildTrigger} />
          </span>
          <span className="branch-build-header__users-for-build-wrapper">
            <UsersForBuild branchBuild={branchBuild} />
          </span>
          <span className="branch-build-header__commits-wrapper">
            <CommitsSummary commitInfo={commitInfo} buildId={buildId} />
          </span>
          {isActiveBuild && (
            <span className="branch-build-header__module-builds-completed visible-lg-block">
              Building … {completedModuleBuildCount}/{totalNonSkippedModuleBuildCount} modules complete
            </span>
          )}
          <span className="branch-build-header__action-items">
            <CancelBuildButton
              onCancel={onCancelBuild}
              build={branchBuild.toJS()}
              btnStyle="link"
              btnClassName="cancel-build-button-link"
            />
          </span>
        </div>
      </div>
    </div>
  );
};

BranchBuildHeader.propTypes = {
  branchBuild: ImmutablePropTypes.mapContains({
    buildNumber: PropTypes.number.isRequired,
    buildTrigger: ImmutablePropTypes.map.isRequired,
    commitInfo: ImmutablePropTypes.map.isRequired
  }),
  completedModuleBuildCount: PropTypes.number.isRequired,
  totalNonSkippedModuleBuildCount: PropTypes.number.isRequired,
  onCancelBuild: PropTypes.func.isRequired
};

export default BranchBuildHeader;
