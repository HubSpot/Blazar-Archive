import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import CardStack from '../shared/card-stack/CardStack.jsx';
import Card from '../shared/card-stack/Card.jsx';

import BranchBuildHeader from './BranchBuildHeader.jsx';
import ModuleItem from './ModuleItem.jsx';
import ModuleBuildHistory from './module-build-history/ModuleBuildHistory.jsx';
import { getCurrentBranchBuild } from '../Helpers';

const ModuleList = ({modules, onItemClick, selectedModuleId}) => {
  const modulesGroupedByCurrentBuild = modules.groupBy((moduleState) =>
    getCurrentBranchBuild(moduleState));

  return (
    <div>
      {modulesGroupedByCurrentBuild.map((moduleStates, branchBuild) => {
        const buildNumber = branchBuild.get('buildNumber');

        return (
          <div className="module-list-group" key={buildNumber}>
            <BranchBuildHeader branchBuild={branchBuild} />
            <CardStack className="module-list-card-stack" key={buildNumber}>
              {moduleStates.map(moduleState => {
                const id = moduleState.getIn(['module', 'id']);
                const moduleName = moduleState.getIn(['module', 'name']);
                const summary = <ModuleItem moduleState={moduleState} onClick={() => onItemClick(id)} />;
                const details = <ModuleBuildHistory moduleName={moduleName} moduleId={id} />;
                const isSelected = selectedModuleId === id;
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
  selectedModuleId: PropTypes.number
};

export default ModuleList;
