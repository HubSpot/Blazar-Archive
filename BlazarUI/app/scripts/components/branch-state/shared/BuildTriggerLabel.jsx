import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import BuildTriggerTypes from '../../../constants/BuildTriggerTypes';

import Tooltip from 'react-bootstrap/lib/Tooltip';
import OverlayTrigger from 'react-bootstrap/lib/OverlayTrigger';

const BuildTriggerLabel = ({buildTrigger}) => {
  switch (buildTrigger.get('type')) {
    case BuildTriggerTypes.PUSH:
      return <span className="build-trigger-label build-trigger-label--code-push">code push</span>;
    case BuildTriggerTypes.MANUAL: {
      const user = buildTrigger.get('id');
      const tooltip = (
        <Tooltip id="historical-deploy-permalink-tooltip">
          Triggered by {user === 'unknown' ? 'unknown user' : user}
        </Tooltip>
      );

      return (
        <OverlayTrigger placement="bottom" overlay={tooltip}>
          <span className="build-trigger-label build-trigger-label--manual">manual</span>
        </OverlayTrigger>
      );
    }
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
