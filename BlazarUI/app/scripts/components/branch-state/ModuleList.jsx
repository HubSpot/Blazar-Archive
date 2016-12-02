import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';

import BranchBuildHeader from './BranchBuildHeader.jsx';
import ModuleItem from './ModuleItem.jsx';
import { getCurrentBranchBuild, getCurrentModuleBuild } from '../Helpers';
import { isComplete as isModuleBuildComplete } from '../../constants/ModuleBuildStates';

const ModuleList = ({modules, onCancelBuild}) => {
  const modulesGroupedByCurrentBuild = modules.groupBy((moduleState) =>
    getCurrentBranchBuild(moduleState));

  return (
    <div>
      {modulesGroupedByCurrentBuild.map((moduleStates, branchBuild) => {
        const buildNumber = branchBuild.get('buildNumber');
        const completedModuleBuildCount = moduleStates.count((moduleState) => {
          const currentModuleBuildState = getCurrentModuleBuild(moduleState).get('state');
          return isModuleBuildComplete(currentModuleBuildState);
        });

        return (
          <div className="module-list-group" key={buildNumber}>
            <BranchBuildHeader
              branchBuild={branchBuild}
              completedModuleBuildCount={completedModuleBuildCount}
              totalNonSkippedModuleBuildCount={moduleStates.size}
              onCancelBuild={onCancelBuild}
            />
            <ul className="list-unstyled" key={buildNumber}>
              {moduleStates.map(moduleState => {
                const id = moduleState.getIn(['module', 'id']);
                return <ModuleItem moduleState={moduleState} key={id} />;
              })}
            </ul>
          </div>
        );
      }).toArray()}
    </div>
  );
};

ModuleList.propTypes = {
  modules: ImmutablePropTypes.list,
  onCancelBuild: PropTypes.func.isRequired
};

export default ModuleList;
