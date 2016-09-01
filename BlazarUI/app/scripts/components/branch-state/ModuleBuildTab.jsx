import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import classNames from 'classnames';

import { getClassNameColorModifier } from '../../constants/ModuleBuildStates';

const ModuleBuildTab = ({moduleBuild, active, onClick}) => {
  const buildNumber = moduleBuild.get('buildNumber');
  const colorModifier = getClassNameColorModifier(moduleBuild.get('state'));
  const tabClasses = classNames('module-build-tab', `module-build-tab--${colorModifier}`, {
    'module-build-tab--active': active
  });

  if (!active) {
    return <div className={tabClasses} onClick={onClick}>#{buildNumber}</div>;
  }

  const arrowClasses = classNames('module-build-tab__arrow',
    `module-build-tab__arrow--${colorModifier}`);

  return (
    <div className="module-build-tab__wrapper">
      <div className={tabClasses}>#{buildNumber}</div>
      <div className={arrowClasses} />
    </div>
  );
};

ModuleBuildTab.propTypes = {
  moduleBuild: ImmutablePropTypes.map,
  active: PropTypes.bool.isRequired,
  onClick: PropTypes.func.isRequired
};

export default ModuleBuildTab;
