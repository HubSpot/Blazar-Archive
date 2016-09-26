import React, { PropTypes } from 'react';
import Immutable from 'immutable';
import classNames from 'classnames';
import { sample } from 'underscore';
import { Link } from 'react-router';

import ModuleBuildNumber from '../shared/ModuleBuildNumber.jsx';
import ModuleBuildStatus from '../shared/ModuleBuildStatus.jsx';
import BuildTriggerLabel from '../shared/BuildTriggerLabel.jsx';

import { canViewDetailedModuleBuildInfo, getBlazarModuleBuildPath } from '../../Helpers';
import BuildTriggerTypes from '../../../constants/BuildTriggerTypes';

const renderContent = (moduleBuild, clickable) => {
  const classes = classNames('module-build-history-item', {'module-build-history-item--clickable': clickable});
  return (
    <div className={classes}>
      <ModuleBuildNumber moduleBuild={moduleBuild} className="module-build-history-item__build-number" />
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
      <Link to={linkPath} className="module-build-history-item-link-wrapper">
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
