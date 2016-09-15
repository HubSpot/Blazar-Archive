import React, { PropTypes } from 'react';
import ModuleBuildNumber from './ModuleBuildNumber.jsx';
import ModuleBuildStatus from './ModuleBuildStatus.jsx';

const ModuleBuildHistoryItem = ({moduleBuild}) => {
  return (
    <li>
      <a className="historical-module-build">
        <ModuleBuildNumber moduleBuild={moduleBuild} className="historical-module-build__build-number" />
        <div className="module-build-history-item__status">
          <ModuleBuildStatus moduleBuild={moduleBuild} noIcon={true} />
        </div>
        <span className="build-trigger-label build-trigger-label--code-push">code push</span>
      </a>
    </li>
  );
};

ModuleBuildHistoryItem.propTypes = {
  moduleBuild: PropTypes.object.isRequired
};

export default ModuleBuildHistoryItem;
