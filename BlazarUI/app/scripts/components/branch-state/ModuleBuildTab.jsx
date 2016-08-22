import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import classNames from 'classnames';

import ModuleBuildStates from '../../constants/ModuleBuildStates';

const getStateClass = (moduleBuild) => {
  switch (moduleBuild.get('state')) {
    case ModuleBuildStates.FAILED:
      return 'module-build-tab--failed';
    case ModuleBuildStates.SUCCEEDED:
      return 'module-build-tab--success';
    case ModuleBuildStates.IN_PROGRESS:
      return 'module-build-tab--in-progress';
    default:
      return null;
  }
};

const getArrowStateClass = (moduleBuild) => {
  switch (moduleBuild.get('state')) {
    case ModuleBuildStates.FAILED:
      return 'module-build-tab__arrow--failed';
    case ModuleBuildStates.SUCCEEDED:
      return 'module-build-tab__arrow--success';
    case ModuleBuildStates.IN_PROGRESS:
      return 'module-build-tab__arrow--in-progress';
    default:
      return null;
  }
};

const getTabClasses = (moduleBuild, active = false) => {
  return classNames('module-build-tab', getStateClass(moduleBuild), {
    'module-build-tab--active': active
  });
};

const ModuleBuildTab = ({moduleBuild, active, onClick}) => {
  const buildNumber = moduleBuild.get('buildNumber');
  if (!active) {
    return <div className={getTabClasses(moduleBuild)} onClick={onClick}>#{buildNumber}</div>;
  }

  return (
    <div>
      <div className={getTabClasses(moduleBuild, active)}>#{buildNumber}</div>
      <div className={classNames('module-build-tab__arrow', getArrowStateClass(moduleBuild)) } />
    </div>
  );
};

ModuleBuildTab.propTypes = {
  moduleBuild: ImmutablePropTypes.map,
  active: PropTypes.bool.isRequired,
  onClick: PropTypes.func.isRequired
};

export default ModuleBuildTab;
