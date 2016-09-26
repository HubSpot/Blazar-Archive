import React, { PropTypes } from 'react';
import classNames from 'classnames';
import { getClassNameColorModifier } from '../../../constants/ModuleBuildStates';

const ModuleBuildNumber = ({moduleBuild, className}) => {
  const buildNumber = moduleBuild.get('buildNumber');
  const colorModifier = getClassNameColorModifier(moduleBuild.get('state'));
  const classes = classNames('module-build-number', `module-build-number--${colorModifier}`, className);

  return (
    <div className={classes}>
      #{buildNumber}
    </div>
  );
};

ModuleBuildNumber.propTypes = {
  moduleBuild: PropTypes.object.isRequired,
  className: PropTypes.string
};

export default ModuleBuildNumber;
