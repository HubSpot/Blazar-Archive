import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import BuildTriggerTypes from '../../constants/BuildTriggerTypes';

const BuildTriggerLabel = ({buildTrigger}) => {
  switch (buildTrigger.get('type')) {
    case BuildTriggerTypes.PUSH:
      return <span className="build-trigger-label build-trigger-label--code-push">code push</span>;
    case BuildTriggerTypes.MANUAL:
      return <span className="build-trigger-label build-trigger-label--manual">manual</span>;
    case BuildTriggerTypes.BRANCH_CREATION:
      return <span className="build-trigger-label build-trigger-label--new-branch">new branch</span>;
    case BuildTriggerTypes.INTER_PROJECT:
      return <span className="build-trigger-label build-trigger-label--dependency">dependency</span>;
    default:
      return <span className="build-trigger-label">{buildTrigger.get('type')}</span>;
  }
};

BuildTriggerLabel.propTypes = {
  buildTrigger: ImmutablePropTypes.mapContains({
    type: PropTypes.oneOf(Object.keys(BuildTriggerTypes)).isRequired,
    id: PropTypes.string.isRequired
  })
};

export default BuildTriggerLabel;
