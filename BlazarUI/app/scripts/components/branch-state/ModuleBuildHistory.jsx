import React, { PropTypes } from 'react';
import ModuleBuildHistoryItem from './ModuleBuildHistoryItem.jsx';
import ModuleBuildHistoryPagination from './ModuleBuildHistoryPagination.jsx';

const ModuleBuildHistory = ({moduleName, moduleId}) => {
  return (
    <div className="module-build-history">
      <h5>Recent builds</h5>
      <ul className="historical-module-build-list">
        <ModuleBuildHistoryItem />
        <ModuleBuildHistoryItem />
        <ModuleBuildHistoryItem />
        <ModuleBuildHistoryItem />
        <ModuleBuildHistoryItem />
      </ul>
      <nav className="text-center" aria-label={`${moduleName} build history`}>
        <ModuleBuildHistoryPagination moduleId={moduleId} />
      </nav>
    </div>
  );
};

ModuleBuildHistory.propTypes = {
  moduleName: PropTypes.string.isRequired,
  moduleId: PropTypes.number.isRequired
};

export default ModuleBuildHistory;
