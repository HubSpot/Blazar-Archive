import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';

import ModuleBuildStatus from './ModuleBuildStatus.jsx';
import BuildTriggerLabel from './BuildTriggerLabel.jsx';
import CommitInfo from './CommitInfo.jsx';

import { getClassNameColorModifier } from '../../constants/ModuleBuildStates';
import BuildTriggerTypes from '../../constants/BuildTriggerTypes';

const ModuleBuild = ({module, moduleBuild, repoBuild, onClick}) => {
  const colorModifier = getClassNameColorModifier(moduleBuild.get('state'));
  const buildTrigger = repoBuild.get('buildTrigger');
  const showCommitInfo = buildTrigger.get('type') === BuildTriggerTypes.PUSH;
  return (
    <div className={`module-build  module-build--${colorModifier}`} onClick={onClick}>
      <div className="module-build__labels">
        <BuildTriggerLabel buildTrigger={buildTrigger} />
        {showCommitInfo && <CommitInfo commitInfo={repoBuild.get('commitInfo')} />}
      </div>
      <h3 className="module-build__module-name">{module.get('name')}</h3>
      <ModuleBuildStatus moduleBuild={moduleBuild} />
    </div>
  );
};

ModuleBuild.propTypes = {
  module: ImmutablePropTypes.map,
  moduleBuild: ImmutablePropTypes.map,
  repoBuild: ImmutablePropTypes.map,
  onClick: PropTypes.func
};

export default ModuleBuild;
