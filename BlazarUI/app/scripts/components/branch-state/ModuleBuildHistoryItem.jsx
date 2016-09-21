import React, { PropTypes } from 'react';
import Immutable from 'immutable';
import classNames from 'classnames';
import { sample } from 'underscore';
import { Link } from 'react-router';

import ModuleBuildNumber from './ModuleBuildNumber.jsx';
import ModuleBuildStatus from './ModuleBuildStatus.jsx';
import BuildTriggerLabel from './BuildTriggerLabel.jsx';

import { canViewDetailedModuleBuildInfo, getBlazarModuleBuildPath } from '../Helpers';
import BuildTriggerTypes from '../../constants/BuildTriggerTypes';

const renderContent = (moduleBuild, clickable) => {
  const classes = classNames('historical-module-build', {'historical-module-build--clickable': clickable});
  return (
    <div className={classes}>
      <ModuleBuildNumber moduleBuild={moduleBuild} className="historical-module-build__build-number" />
        <div className="module-build-history-item__status">
          <ModuleBuildStatus moduleBuild={moduleBuild} noIcon={true} />
        </div>
        <div className="module-build-history-item__build-trigger-label">
          <BuildTriggerLabel buildTrigger={Immutable.Map({
            type: sample(Object.keys(BuildTriggerTypes)),
            id: 'Mock'
          })}
          />
        </div>
    </div>
  );
};

const ModuleBuildHistoryItem = ({moduleBuild, moduleName, branchId}) => {
  if (!canViewDetailedModuleBuildInfo(moduleBuild)) {
    return <li>{renderContent(moduleBuild)}</li>;
  }

  const linkPath = getBlazarModuleBuildPath(branchId, moduleBuild.get('buildNumber'), moduleName);
  return (
    <li>
      <Link to={linkPath} className="historical-module-build-link-wrapper">
        {renderContent(moduleBuild, true)}
      </Link>
    </li>
  );
};

ModuleBuildHistoryItem.propTypes = {
  moduleBuild: PropTypes.object.isRequired,
  moduleName: PropTypes.string.isRequired,
  branchId: PropTypes.number.isRequired
};

export default ModuleBuildHistoryItem;
