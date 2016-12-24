import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';

import BranchBuildHeader from './BranchBuildHeader.jsx';
import ModuleItem from './ModuleItem.jsx';
import { getCurrentBranchBuild, getCurrentModuleBuild } from '../Helpers';
import { isComplete as isModuleBuildComplete } from '../../constants/ModuleBuildStates';

const sortModules = (modules, branchBuild) => {
  const topologicalSort = branchBuild.getIn(['dependencyGraph', 'topologicalSort']);

  // gracefully handling old builds without topological sort info
  if (!topologicalSort) {
    return modules;
  }

  // sort by dependency order so that module builds proceed from top to bottom
  return modules.sort((a, b) => {
    const indexA = topologicalSort.indexOf(a.getIn(['module', 'id']));
    const indexB = topologicalSort.indexOf(b.getIn(['module', 'id']));
    return indexA - indexB;
  });
};

const ModuleList = ({modules, onCancelBuild}) => {
  const modulesGroupedByCurrentBuild = modules.groupBy((moduleState) =>
    getCurrentBranchBuild(moduleState));

  // reverse chronological order
  const sortedBranchBuildEvents = modulesGroupedByCurrentBuild.keySeq().toList()
    .sort((branchBuildA, branchBuildB) => {
      return branchBuildB.get('buildNumber') - branchBuildA.get('buildNumber');
    });

  return (
    <div>
      {sortedBranchBuildEvents.map((branchBuild) => {
        const moduleStates = sortModules(modulesGroupedByCurrentBuild.get(branchBuild), branchBuild);
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
