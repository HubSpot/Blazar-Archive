import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';

import ModuleItemSummary from './ModuleItemSummary.jsx';

const ModuleItem = ({module, currentModuleBuild, currentBranchBuild, isExpanded, onClick}) => {
  return (
    <li className="module-item">
      <ModuleItemSummary
        module={module}
        currentModuleBuild={currentModuleBuild}
        currentBranchBuild={currentBranchBuild}
        isExpanded={isExpanded}
        onClick={onClick}
      />
    </li>
  );
};

ModuleItem.propTypes = {
  module: ImmutablePropTypes.map,
  currentModuleBuild: ImmutablePropTypes.map,
  currentBranchBuild: ImmutablePropTypes.map,
  isExpanded: PropTypes.bool.isRequired,
  onClick: PropTypes.func.isRequired
};

export default ModuleItem;
