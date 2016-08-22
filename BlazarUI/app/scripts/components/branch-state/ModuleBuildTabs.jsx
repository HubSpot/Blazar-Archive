import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import classNames from 'classnames';

import ModuleBuildTab from './ModuleBuildTab.jsx';
import ModuleBuildStates from '../../constants/ModuleBuildStates';

const getTabClasses = (moduleBuild, selectedBuildNumber) => {
  const isSelected = moduleBuild.get('buildNumber') === selectedBuildNumber;
  const state = moduleBuild.get('state');
  return classNames('module-build-tab', {
    'module-build-tab--failed': state === ModuleBuildStates.FAILED,
    'module-build-tab--success': state === ModuleBuildStates.SUCCEEDED,
    'module-build-tab--in-progress': state === ModuleBuildStates.IN_PROGRESS,
    'module-build-tab--active': isSelected
  });
};

const ModuleBuildTabs = ({moduleState, selectedBuildNumber, onSelectModuleBuild}) => {
  const lastNonSkippedBuild = moduleState.get('lastNonSkippedBuild');
  const lastNonSkippedBuildNumber = lastNonSkippedBuild.get('buildNumber');

  const lastSuccessfulBuild = moduleState.get('lastSuccessfulBuild');
  const lastSuccessfulBuildNumber = lastSuccessfulBuild.get('buildNumber');

  if (lastNonSkippedBuildNumber === lastSuccessfulBuildNumber) {
    const buildNumberClasses = 'module-item-summary__build-number module-item-summary__build-number--success';
    return <div className={buildNumberClasses}>#{lastSuccessfulBuildNumber}</div>;
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
