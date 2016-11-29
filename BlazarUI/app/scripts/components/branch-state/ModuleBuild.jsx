import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import { Link } from 'react-router';
import classNames from 'classnames';

import ModuleBuildStatus from './shared/ModuleBuildStatus.jsx';
import Icon from '../../components/shared/Icon.jsx';

import { getClassNameColorModifier } from '../../constants/ModuleBuildStates';
import { canViewDetailedModuleBuildInfo, getBlazarModuleBuildPath } from '../Helpers';

const getModuleName = (module, moduleBuild, branchBuild) => {
  const moduleName = module.get('name');
  if (canViewDetailedModuleBuildInfo(moduleBuild)) {
    const linkPath = getBlazarModuleBuildPath(branchBuild.get('branchId'), moduleBuild.get('buildNumber'), moduleName);
    return <Link to={linkPath}>{moduleName}</Link>;
  }

  return moduleName;
};

const ModuleBuild = ({module, moduleBuild, branchBuild, isExpanded, onClick}) => {
  const colorModifier = getClassNameColorModifier(moduleBuild.get('state'));

  const expandIndicatorIcon = isExpanded ? 'angle-up' : 'angle-down';
  const expandIndicatorClassNames = classNames(
    'fa-3x',
    'module-build__expand-indicator',
    {'module-build__expand-indicator--expanded': isExpanded}
  );

  const handleClick = (e) => {
    if (e.target.tagName !== 'A') {
      onClick();
    }
  };
  return (
    <div className={`module-build  module-build--${colorModifier}`} onClick={handleClick}>
      <Icon name={expandIndicatorIcon} classNames={expandIndicatorClassNames} />
      <h3 className="module-build__module-name">{getModuleName(module, moduleBuild, branchBuild)}</h3>
      <ModuleBuildStatus moduleBuild={moduleBuild} />
    </div>
  );
};

ModuleBuild.propTypes = {
  module: ImmutablePropTypes.map,
  moduleBuild: ImmutablePropTypes.map,
  branchBuild: ImmutablePropTypes.map,
  isExpanded: PropTypes.bool.isRequired,
  onClick: PropTypes.func
};

export default ModuleBuild;
