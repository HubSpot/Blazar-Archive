import React, {Component, PropTypes} from 'react';
import Module from '../sidebar/Module.jsx';

import {filter, map, max} from 'underscore';
import Helpers from '../ComponentHelpers';
import Collapsable from '../shared/Collapsable.jsx';
import MutedMessage from '../shared/MutedMessage.jsx';

class DashboardRepoDetails extends Component {

  constructor(props) {
    super(props);
  }

  render() {
    let repo = this.props.repo;
    let buildingModuleList = [];
    let inactiveModuleList = [];
    let inactivePanel, activePanel;

    filter(repo.modules, (m) => {
      return m.inProgressBuild;
    }).forEach((build) => {
      buildingModuleList.push(
        <Module key={build.modulePath} repo={build} />
      );
    });

    filter(repo.modules, (m) => {
      return !m.inProgressBuild;
    }).forEach((build) => {
      inactiveModuleList.push(
        <Module key={build.modulePath} repo={build} />
      );
    });

    let activePanelHeader = `Active Builds (${buildingModuleList.length})`;
    let inactivePanelHeader = `Inactive Builds (${inactiveModuleList.length})`;

    if (inactiveModuleList.length > 0) {
      inactivePanel = (
        <Collapsable header={inactivePanelHeader}>
          {inactiveModuleList}
        </Collapsable>
      )
    }

    if (buildingModuleList.length > 0) {
      activePanel = (
        <Collapsable header={activePanelHeader}>
          {buildingModuleList}
        </Collapsable>
      )
    }

    let latest = Helpers.timestampFormatted(max(map(repo.modules, (m) => {
      if (m.inProgressBuild) {
        return m.inProgressBuild.startTimestamp;
      } else if (m.lastBuild) {
        return m.lastBuild.endTimestamp;
      }
      return 0;
    })));

    return (
      <div>
        <MutedMessage>
          Last Activity: {latest}
        </MutedMessage>
        {activePanel}
        {inactivePanel}
      </div>
    );
  }
}

DashboardRepoDetails.propTypes = {
  repo: PropTypes.object
};

export default DashboardRepoDetails;
