import React from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import ModuleItem from './ModuleItem.jsx';

const ModuleList = ({modules}) => {
  return (
    <ul>
      {modules.map(moduleState => {
        const id = moduleState.getIn(['module', 'id']);
        return <ModuleItem moduleState={moduleState} key={id} />;
      })}
    </ul>
  );
};

ModuleList.propTypes = {
  modules: ImmutablePropTypes.list
};

export default ModuleList;
