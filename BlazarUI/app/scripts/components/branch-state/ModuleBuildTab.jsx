import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import classNames from 'classnames';

import ModuleBuildNumber from './ModuleBuildNumber.jsx';
import { getClassNameColorModifier } from '../../constants/ModuleBuildStates';

const ModuleBuildTab = ({moduleBuild, active, onClick}) => {
  const colorModifier = getClassNameColorModifier(moduleBuild.get('state'));
  const tabClasses = classNames('module-build-tab', `module-build-tab--${colorModifier}`, {
    'module-build-tab--inactive': !active
  });

  if (!active) {
    return (
      <div className="module-build-tab__wrapper" onClick={onClick}>
        <ModuleBuildNumber className={tabClasses} moduleBuild={moduleBuild} />
      </div>
    );
  }

  const arrowClasses = classNames('module-build-tab__arrow',
    `module-build-tab__arrow--${colorModifier}`);

  return (
    <div className="module-build-tab__wrapper">
      <ModuleBuildNumber className={tabClasses} moduleBuild={moduleBuild} />
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
