import React from 'react';
import ModuleBuildHistoryItem from './ModuleBuildHistoryItem.jsx';

const ModuleBuildHistory = () => {
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
    </div>
  );
};

export default ModuleBuildHistory;
