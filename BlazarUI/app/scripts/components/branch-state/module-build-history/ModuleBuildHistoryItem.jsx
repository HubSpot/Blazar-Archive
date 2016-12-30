import React, { PropTypes } from 'react';
import {Link} from 'react-router';
import classNames from 'classnames';

import ModuleBuildStatus from '../shared/ModuleBuildStatus.jsx';
import BuildTriggerLabel from '../shared/BuildTriggerLabel.jsx';
import UsersForBuild from '../shared/UsersForBuild.jsx';
import CommitsSummary from '../shared/CommitsSummary.jsx';
import ModuleBuildListItemWrapper from '../shared/ModuleBuildListItemWrapper.jsx';

import { canViewDetailedModuleBuildInfo } from '../../Helpers';
import { getModuleBuildPath } from '../../../utils/blazarPaths';
import { getClassNameColorModifier } from '../../../constants/ModuleBuildStates';

const renderBuildNumber = (moduleName, moduleBuild, branchBuild) => {
  const branchId = branchBuild.get('branchId');
  const buildNumber = moduleBuild.get('buildNumber');
  const formattedBuildNumber = `#${buildNumber}`;

  if (canViewDetailedModuleBuildInfo(moduleBuild)) {
    const linkPath = getModuleBuildPath(branchId, buildNumber, moduleName);
    return (
      <Link to={linkPath} className="module-build-history-item__build-log-link">
        {formattedBuildNumber}
      </Link>
    );
  }

  return formattedBuildNumber;
};

const ModuleBuildHistoryItem = ({moduleBuild, moduleName, branchBuild}) => {
  const colorModifier = getClassNameColorModifier(moduleBuild.get('state'));
  const divClassName = classNames('module-build-history-item', {
    [`module-build-history-item--${colorModifier}`]: colorModifier
  });

  return (
    <li>
      <ModuleBuildListItemWrapper moduleBuild={moduleBuild}>
        <div className={divClassName}>
          <div className="module-build-history-item__build-number">
            {renderBuildNumber(moduleName, moduleBuild, branchBuild)}
          </div>
          <div className="module-build-history-item__status">
            <ModuleBuildStatus moduleBuild={moduleBuild} noIcon={true} abbreviateUnitsBreakpoint={250} />
          </div>
          <div className="module-build-history-item__build-trigger-label">
            <BuildTriggerLabel buildTrigger={branchBuild.get('buildTrigger')} />
          </div>
          <div className="module-build-history-item__users-for-build-wrapper">
            <UsersForBuild branchBuild={branchBuild} />
          </div>
          <div className="module-build-history-item__commits-wrapper">
            <CommitsSummary
              commitInfo={branchBuild.get('commitInfo')}
              buildId={branchBuild.get('id')}
              popoverPlacement="left"
            />
          </div>
        </div>
      </ModuleBuildListItemWrapper>
    </li>
  );
};

ModuleBuildHistoryItem.propTypes = {
  moduleBuild: PropTypes.object.isRequired,
  moduleName: PropTypes.string.isRequired,
  branchBuild: PropTypes.object.isRequired
};

export default ModuleBuildHistoryItem;
