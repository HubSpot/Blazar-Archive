import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';

import ModuleBuildNumber from './shared/ModuleBuildNumber.jsx';
import ModuleBuildTab from './ModuleBuildTab.jsx';

const ModuleBuildTabs = ({currentBuild, lastSuccessfulBuild, selectedBuildNumber, onSelectModuleBuild}) => {
  const currentBuildNumber = currentBuild.get('buildNumber');
  const lastSuccessfulBuildNumber = lastSuccessfulBuild ? lastSuccessfulBuild.get('buildNumber') : null;

  if (!lastSuccessfulBuild || currentBuildNumber === lastSuccessfulBuildNumber) {
    return <ModuleBuildNumber className="module-item-summary__build-number" moduleBuild={currentBuild} />;
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
