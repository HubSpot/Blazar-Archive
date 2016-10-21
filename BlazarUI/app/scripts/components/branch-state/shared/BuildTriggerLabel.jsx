import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import BuildTriggerTypes from '../../../constants/BuildTriggerTypes';

import Tooltip from 'react-bootstrap/lib/Tooltip';
import OverlayTrigger from 'react-bootstrap/lib/OverlayTrigger';

const BUILD_TRIGGER_LABEL_PROPERTIES = {
  [BuildTriggerTypes.PUSH]: {
    labelModifierClassName: 'build-trigger-label--code-push',
    labelText: 'code push'
  },
  [BuildTriggerTypes.MANUAL]: {
    labelModifierClassName: 'build-trigger-label--manual',
    labelText: 'manual'
  },
  [BuildTriggerTypes.BRANCH_CREATION]: {
    labelModifierClassName: 'build-trigger-label--new-branch',
    labelText: 'new branch'
  },
  [BuildTriggerTypes.INTER_PROJECT]: {
    labelModifierClassName: 'build-trigger-label--dependency',
    labelText: 'dependency'
  }
};

const getTooltipText = (buildTrigger) => {
  switch (buildTrigger.get('type')) {
    case BuildTriggerTypes.PUSH:
      return 'This build was triggered by a commit in GitHub';
    case BuildTriggerTypes.MANUAL: {
      const user = buildTrigger.get('id');
      const isUserUnknown = !user || user === 'unknown';
      return `This build was triggered manually in Blazar by ${isUserUnknown ? 'an unknown user' : user}`;
    }
    case BuildTriggerTypes.BRANCH_CREATION:
      return 'This build was triggered by a new branch in GitHub';
    case BuildTriggerTypes.INTER_PROJECT:
      return 'This build was triggered by the build of an upstream dependency and will trigger the builds of any dependent downstream modules';
    default:
      return '';
  }
};

const BuildTriggerLabel = ({buildTrigger}) => {
  const buildTriggerType = buildTrigger.get('type');

  const labelProperties = BUILD_TRIGGER_LABEL_PROPERTIES[buildTriggerType];
  if (!labelProperties) {
    return <span className="build-trigger-label">{buildTriggerType}</span>;
  }

  const {labelModifierClassName, labelText} = labelProperties;
  const tooltipId = `${buildTriggerType}-build-trigger-label-tooltip`;
  const tooltip = <Tooltip id={tooltipId}>{getTooltipText(buildTrigger)}</Tooltip>;

  return (
    <OverlayTrigger placement="bottom" overlay={tooltip}>
      <span className={`build-trigger-label ${labelModifierClassName}`}>{labelText}</span>
    </OverlayTrigger>
  );
};

BuildTriggerLabel.propTypes = {
  buildTrigger: ImmutablePropTypes.mapContains({
    type: PropTypes.oneOf(Object.keys(BuildTriggerTypes)).isRequired
  })
};

export default BuildTriggerLabel;
