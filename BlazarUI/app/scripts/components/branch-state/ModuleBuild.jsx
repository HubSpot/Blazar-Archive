import React from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';

import ModuleBuildStatus from './ModuleBuildStatus.jsx';

const ModuleBuild = ({module, moduleBuild}) => {
  return (
    <div className="module-item-summary__main">
      <h3 className="module-item-summary__name">{module.get('name')}</h3>
      <ModuleBuildStatus moduleBuild={moduleBuild} />
    </div>
  );
};

ModuleBuild.propTypes = {
  module: ImmutablePropTypes.map,
  moduleBuild: ImmutablePropTypes.map
};

export default ModuleBuild;
