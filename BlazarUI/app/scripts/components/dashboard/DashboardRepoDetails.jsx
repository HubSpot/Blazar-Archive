import React, {Component, PropTypes} from 'react';
import { Link } from 'react-router';
import Module from '../sidebar/Module.jsx';
import PanelGroup from 'react-bootstrap/lib/PanelGroup';
import Panel from 'react-bootstrap/lib/Panel';
import {filter} from 'underscore';

class DashboardRepoDetails extends Component {

  constructor(props) {
    super(props);
    this.state = { modulesOpen: false };
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

    let activePanel = (buildingModuleList.length !== 0 ? <Panel header='In Progress Builds' eventKey='1'>{buildingModuleList}</Panel> : '');
    let inactivePanel = (inactiveModuleList.length !== 0 ? <Panel header='Inactive Builds' eventKey='2'>{inactiveModuleList}</Panel> : '');

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
