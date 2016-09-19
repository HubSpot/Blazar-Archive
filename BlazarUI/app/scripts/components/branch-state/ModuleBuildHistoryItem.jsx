import React, { PropTypes } from 'react';
import Immutable from 'immutable';
import { sample } from 'underscore';

import ModuleBuildNumber from './ModuleBuildNumber.jsx';
import ModuleBuildStatus from './ModuleBuildStatus.jsx';
import BuildTriggerLabel from './BuildTriggerLabel.jsx';
import BuildTriggerTypes from '../../constants/BuildTriggerTypes';

const ModuleBuildHistoryItem = ({moduleBuild}) => {
  return (
    <li>
      <a className="historical-module-build">
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
      </a>
    </li>
  );
};

ModuleBuildHistoryItem.propTypes = {
  moduleBuild: PropTypes.object.isRequired
};

export default ModuleBuildHistoryItem;
