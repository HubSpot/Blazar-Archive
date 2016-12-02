import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import CardStack from '../shared/card-stack/CardStack.jsx';
import Card from '../shared/card-stack/Card.jsx';

import BranchBuildHeader from './BranchBuildHeader.jsx';
import ModuleItem from './ModuleItem.jsx';
import ModuleBuildHistory from './module-build-history/ModuleBuildHistory.jsx';
import { getCurrentBranchBuild, getCurrentModuleBuild } from '../Helpers';
import { isComplete as isModuleBuildComplete } from '../../constants/ModuleBuildStates';

const ModuleList = ({modules, onItemClick, selectedModuleId, onCancelBuild}) => {
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
            <CardStack className="module-list-card-stack" condensed={true} key={buildNumber}>
              {moduleStates.map(moduleState => {
                const id = moduleState.getIn(['module', 'id']);
                const moduleName = moduleState.getIn(['module', 'name']);
                const isSelected = selectedModuleId === id;
                const summary = (
                  <ModuleItem
                    module={moduleState.get('module')}
                    currentModuleBuild={getCurrentModuleBuild(moduleState)}
                    currentBranchBuild={getCurrentBranchBuild(moduleState)}
                    isExpanded={isSelected}
                    onClick={() => onItemClick(id)}
                  />
                );

                const lastSuccessfulBuildNumber = moduleState.getIn(['lastSuccessfulModuleBuild', 'buildNumber']);
                const details = (
                  <ModuleBuildHistory
                    moduleName={moduleName}
                    moduleId={id}
                    lastSuccessfulBuildNumber={lastSuccessfulBuildNumber}
                  />
                );

                return (
                  <Card
                    key={id}
                    summary={summary}
                    details={details}
                    expanded={isSelected}
                  />
                );
              })}
            </CardStack>
          </div>
        );
      }

      ).toArray()}
    </div>
  );
};

ModuleList.propTypes = {
  modules: ImmutablePropTypes.list,
  onItemClick: PropTypes.func.isRequired,
  selectedModuleId: PropTypes.number,
  onCancelBuild: PropTypes.func.isRequired
};

export default ModuleList;
