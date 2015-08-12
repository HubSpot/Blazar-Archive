import React, {Component, PropTypes} from 'react';
import { Link } from 'react-router';
import Module from '../sidebar/Module.jsx';
import PanelGroup from 'react-bootstrap/lib/PanelGroup';
import Panel from 'react-bootstrap/lib/Panel';
import {filter} from 'underscore';

class DashboardRepoDetails extends Component {

  constructor(props) {
    super(props);
  }

  render() {
    let repo = this.props.repo;
    let buildingModuleList = [];
    let inactiveModuleList = [];

    filter(repo.modules, (m) => { return m.inProgressBuild; }).forEach((build) => {
      buildingModuleList.push(
        <Module key={build.modulePath} repo={build} />
      );
    });
    filter(repo.modules, (m) => { return !m.inProgressBuild; }).forEach((build) => {
      inactiveModuleList.push(
        <Module key={build.modulePath} repo={build} />
      );
    });

    let activePanelHeader = `In Progress Builds (${buildingModuleList.length})`;
    let activePanel = (buildingModuleList.length !== 0 ? <Panel header={activePanelHeader} eventKey='1'>{buildingModuleList}</Panel> : '');
    let inactivePanelHeader = `Inactive Builds (${inactiveModuleList.length})`;
    let inactivePanel = (inactiveModuleList.length !== 0 ? <Panel header={inactivePanelHeader} eventKey='2'>{inactiveModuleList}</Panel> : '');

    return (
      <div>
        <PanelGroup accordion>
          {activePanel}
          {inactivePanel}
        </PanelGroup>
      </div>
    );
  }
}

DashboardRepoDetails.propTypes = {
  repo: PropTypes.object
};

export default DashboardRepoDetails;
