import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import { Link } from 'react-router';

import ModuleBuildStatus from './shared/ModuleBuildStatus.jsx';
import BuildTriggerLabel from './shared/BuildTriggerLabel.jsx';
import CommitInfo from './shared/CommitInfo.jsx';

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

const ModuleBuild = ({module, moduleBuild, branchBuild, onClick}) => {
  const colorModifier = getClassNameColorModifier(moduleBuild.get('state'));
  const buildTrigger = branchBuild.get('buildTrigger');
  const commitInfo = branchBuild.get('commitInfo');
  const handleClick = (e) => {
    if (e.target.tagName !== 'A') {
      onClick();
    }
  };
  return (
    <div className={`module-build  module-build--${colorModifier}`} onClick={handleClick}>
      <div className="module-build__labels">
        <BuildTriggerLabel buildTrigger={buildTrigger} />
        <CommitInfo commitInfo={commitInfo} />
      </div>
      <h3 className="module-build__module-name">{getModuleName(module, moduleBuild, branchBuild)}</h3>
      <ModuleBuildStatus moduleBuild={moduleBuild} />
    </div>
  );
};

ModuleBuild.propTypes = {
  module: ImmutablePropTypes.map,
  moduleBuild: ImmutablePropTypes.map,
  branchBuild: ImmutablePropTypes.map,
  onClick: PropTypes.func
};

export default ModuleBuild;
