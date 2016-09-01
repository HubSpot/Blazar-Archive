import React from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';

import ModuleBuildStatus from './ModuleBuildStatus.jsx';
import { getClassNameColorModifier } from '../../constants/ModuleBuildStates';

const ModuleBuild = ({module, moduleBuild}) => {
  const colorModifier = getClassNameColorModifier(moduleBuild.get('state'));
  return (
    <div className={`module-build  module-build--${colorModifier}`}>
      <h3 className="module-build__module-name">{module.get('name')}</h3>
      <ModuleBuildStatus moduleBuild={moduleBuild} />
    </div>
  );
};

ModuleBuild.propTypes = {
  module: ImmutablePropTypes.map,
  moduleBuild: ImmutablePropTypes.map
};

export default ModuleBuild;
