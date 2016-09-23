import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';

import ModuleBuildNumber from './ModuleBuildNumber.jsx';
import ModuleBuildTab from './ModuleBuildTab.jsx';

const ModuleBuildTabs = ({currentBuild, lastSuccessfulBuild, selectedBuildNumber, onSelectModuleBuild}) => {
  const currentBuildNumber = currentBuild.get('buildNumber');
  const lastSuccessfulBuildNumber = lastSuccessfulBuild.get('buildNumber');

  if (currentBuildNumber === lastSuccessfulBuildNumber) {
    return <ModuleBuildNumber className="module-item-summary__build-number" moduleBuild={lastSuccessfulBuild} />;
  }

  return (
    <div className="module-build-tabs">
      <ModuleBuildTab
        moduleBuild={currentBuild}
        active={currentBuildNumber === selectedBuildNumber}
        onClick={() => onSelectModuleBuild(currentBuild)}
      />
      <ModuleBuildTab
        moduleBuild={lastSuccessfulBuild}
        active={lastSuccessfulBuildNumber === selectedBuildNumber}
        onClick={() => onSelectModuleBuild(lastSuccessfulBuild)}
      />
    </div>
  );
};

ModuleBuildTabs.propTypes = {
  currentBuild: ImmutablePropTypes.map,
  lastSuccessfulBuild: ImmutablePropTypes.map,
  selectedBuildNumber: PropTypes.number.isRequired,
  onSelectModuleBuild: PropTypes.func.isRequired
};

export default ModuleBuildTabs;
