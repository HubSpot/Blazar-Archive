import React, { PropTypes } from 'react';
import {Link} from 'react-router';

import ModuleBuildStatus from '../shared/ModuleBuildStatus.jsx';
import BuildTriggerLabel from '../shared/BuildTriggerLabel.jsx';
import UsersForBuild from '../shared/UsersForBuild.jsx';
import CommitsSummary from '../shared/CommitsSummary.jsx';

import { canViewDetailedModuleBuildInfo, getBlazarModuleBuildPath } from '../../Helpers';

const ModuleBuildHistoryItem = ({moduleBuild, moduleName, branchBuild}) => {
  const branchId = branchBuild.get('branchId');
  const buildNumber = moduleBuild.get('buildNumber');
  const formattedBuildNumber = `#${buildNumber}`;

  const linkPath = getBlazarModuleBuildPath(branchId, buildNumber, moduleName);

  const commitInfo = branchBuild.get('commitInfo');

  return (
    <li>
      <div className={"module-build-history-item"}>
        <div className="module-build-history-item__build-number">
          {canViewDetailedModuleBuildInfo(moduleBuild) ?
            <Link to={linkPath}>{formattedBuildNumber}</Link> : formattedBuildNumber
          }
        </div>
        <div className="module-build-history-item__status">
          <ModuleBuildStatus moduleBuild={moduleBuild} noIcon={true} />
        </div>
        <div className="module-build-history-item__build-trigger-label">
          <BuildTriggerLabel buildTrigger={branchBuild.get('buildTrigger')} />
        </div>
        <div className="module-build-history-item__users-for-build-wrapper">
          <UsersForBuild branchBuild={branchBuild} />
        </div>
        <div className="module-build-history-item__commits-wrapper">
          <CommitsSummary commitInfo={commitInfo} buildId={branchBuild.get('id')} popoverPlacement="left" />
        </div>
      </div>
    </li>
  );
};

ModuleBuildHistoryItem.propTypes = {
  moduleBuild: PropTypes.object.isRequired,
  moduleName: PropTypes.string.isRequired,
  branchBuild: PropTypes.object.isRequired
};

export default ModuleBuildHistoryItem;
