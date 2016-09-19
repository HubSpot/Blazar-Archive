import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';

import ModuleBuildNumber from './ModuleBuildNumber.jsx';
import ModuleBuildTab from './ModuleBuildTab.jsx';

const ModuleBuildTabs = ({moduleState, selectedBuildNumber, onSelectModuleBuild}) => {
  const lastNonSkippedBuild = moduleState.get('lastNonSkippedModuleBuild');
  const lastNonSkippedBuildNumber = lastNonSkippedBuild.get('buildNumber');

  const lastSuccessfulBuild = moduleState.get('lastSuccessfulModuleBuild');
  const lastSuccessfulBuildNumber = lastSuccessfulBuild.get('buildNumber');

  if (lastNonSkippedBuildNumber === lastSuccessfulBuildNumber) {
    return <ModuleBuildNumber className="module-item-summary__build-number" moduleBuild={lastSuccessfulBuild} />;
  }

  return (
    <div className="module-build-tabs">
      <ModuleBuildTab
        moduleBuild={lastNonSkippedBuild}
        active={lastNonSkippedBuildNumber === selectedBuildNumber}
        onClick={() => onSelectModuleBuild(lastNonSkippedBuild)}
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
  moduleState: ImmutablePropTypes.map,
  selectedBuildNumber: PropTypes.number.isRequired,
  onSelectModuleBuild: PropTypes.func.isRequired
};

export default ModuleBuildTabs;
