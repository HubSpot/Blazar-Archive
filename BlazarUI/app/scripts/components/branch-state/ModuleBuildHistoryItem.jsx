import React from 'react';

const ModuleBuildHistory = () => {
  return (
    <li>
      <a className="historical-module-build">
        <div className="historical-module-build__build-number">
          #37
        </div>
        <p className="module-build-history-item__status">Built 2 hours ago in 1 minute 25 seconds</p>
        <span className="build-trigger-label build-trigger-label--code-push">code push</span>
      </a>
    </li>
  );
};

export default ModuleBuildHistory;
