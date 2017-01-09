import React, { PropTypes } from 'react';
import ImmutablePropTypes from 'react-immutable-proptypes';
import { Link } from 'react-router';
import classNames from 'classnames';

import ModuleBuildStatus from './shared/ModuleBuildStatus.jsx';
import Icon from '../shared/Icon.jsx';
import ModuleBuildListItemWrapper from './shared/ModuleBuildListItemWrapper.jsx';
import SingularityLink from './shared/SingularityLink.jsx';

import { getClassNameColorModifier } from '../../constants/ModuleBuildStates';
import { canViewDetailedModuleBuildInfo } from '../Helpers';
import { getModuleBuildPath } from '../../utils/blazarPaths';
import isDebugMode from '../../utils/isDebugMode';

const getBuildLogLink = (module, moduleBuild, branchBuild) => {
  if (canViewDetailedModuleBuildInfo(moduleBuild)) {
    const moduleName = module.get('name');
    const linkPath = getModuleBuildPath(branchBuild.get('branchId'), moduleBuild.get('buildNumber'), moduleName);
    const showSingularityLink = isDebugMode();
    return (
      <div className="module-item-summary__build-log-link-container">
        <Link to={linkPath} className="module-item-summary__build-log-link">View build log</Link>
        {showSingularityLink && ' | '}
        {showSingularityLink && <SingularityLink taskId={moduleBuild.get('taskId')} />}
      </div>
    );
  }

  return null;
};

const ModuleItemSummary = ({module, currentModuleBuild, currentBranchBuild, isExpanded, onClick}) => {
  const colorModifier = getClassNameColorModifier(currentModuleBuild.get('state'));

  const divClassName = classNames('module-item-summary', {
    [`module-item-summary--${colorModifier}`]: colorModifier
  });

  const expandIndicatorIcon = isExpanded ? 'angle-up' : 'angle-down';
  const expandIndicatorClassName = classNames(
    'fa-3x',
    'module-item-summary__expand-indicator',
    {'module-item-summary__expand-indicator--expanded': isExpanded}
  );

  const handleClick = (e) => {
    if (e.target.tagName !== 'A') {
      onClick();
    }
  };

  return (
    <ModuleBuildListItemWrapper moduleBuild={currentModuleBuild}>
      <div className={divClassName} onClick={handleClick}>
        <Icon name={expandIndicatorIcon} classNames={expandIndicatorClassName} />
        <h3 className="module-item-summary__module-name">{module.get('name')}</h3>
        <ModuleBuildStatus moduleBuild={currentModuleBuild} abbreviateUnitsBreakpoint={325} />
        {getBuildLogLink(module, currentModuleBuild, currentBranchBuild)}
      </div>
    </ModuleBuildListItemWrapper>
  );
};

ModuleItemSummary.propTypes = {
  module: ImmutablePropTypes.map,
  currentModuleBuild: ImmutablePropTypes.map,
  currentBranchBuild: ImmutablePropTypes.map,
  isExpanded: PropTypes.bool.isRequired,
  onClick: PropTypes.func
};

export default ModuleItemSummary;
