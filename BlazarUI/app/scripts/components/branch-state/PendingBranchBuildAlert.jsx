import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';

import Alert from '../shared/Alert.jsx';
import CancelBuildButton from '../shared/branch-build/CancelBuildButton.jsx';
import BuildTriggerLabel from './shared/BuildTriggerLabel.jsx';

const PendingBranchBuildsAlert = ({branchBuild, onCancelBuild}) => {
  const id = branchBuild.get('id');
  const buildNumber = branchBuild.get('buildNumber');
  const state = branchBuild.get('state');

  return (
    <Alert className="pending-branch-build-alert" type="info" key={id}>
      <div className="pending-branch-build-alert__content">
        <span className="pending-branch-build-alert__build-number">#{buildNumber}</span>
        <BuildTriggerLabel buildTrigger={branchBuild.get('buildTrigger')} />
        <span className="pending-branch-build-alert__build-state">
          is {state.toLowerCase()}
        </span>
        <CancelBuildButton
          onCancel={onCancelBuild}
          build={branchBuild.toJS()}
          btnSize="xs"
          btnStyle="link"
          btnClassName="cancel-build-button-link"
        />
      </div>
    </Alert>
  );
};

PendingBranchBuildsAlert.propTypes = {
  branchBuild: ImmutablePropTypes.mapContains({
    id: PropTypes.number.isRequired,
    buildNumber: PropTypes.number.isRequired,
    buildTrigger: ImmutablePropTypes.map.isRequired
  }),
  onCancelBuild: PropTypes.func.isRequired
};

export default PendingBranchBuildsAlert;
