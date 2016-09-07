import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import CardStack from '../shared/card-stack/CardStack.jsx';
import Card from '../shared/card-stack/Card.jsx';

import ModuleItem from './ModuleItem.jsx';
import ModuleBuildHistory from './ModuleBuildHistory.jsx';

const ModuleList = ({modules, onItemClick, selectedModuleId}) => {
  return (
    <CardStack>
      {modules.map(moduleState => {
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
  );
};

ModuleList.propTypes = {
  modules: ImmutablePropTypes.list,
  onItemClick: PropTypes.func.isRequired,
  selectedModuleId: PropTypes.number
};

export default ModuleList;
