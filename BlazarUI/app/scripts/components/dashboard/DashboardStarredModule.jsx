/* To do:
    - Notifications
    - Progress bar
    <NotificationToggle repo={gitInfo.repository} branch={gitInfo.branch}></NotificationToggle>
*/

/*global config*/
import React, {Component, PropTypes} from 'react';
import { Link } from 'react-router';
import Icon from '../shared/Icon.jsx';
import EmptyMessage from '../shared/EmptyMessage.jsx';
import NotificationToggle from '../shared/NotificationToggle.jsx';
import BuildHistoryTable from '../module/BuildHistoryTable.jsx';
import Collapsable from '../shared/Collapsable.jsx';

class DashboardStarredModule extends Component {

  render() {

    if (this.props.modules.length === 0) {
      // to do -> pass more star detail down as props (e.g. branch)
      const repo = this.props.modulePath.split('/')[4];
      const branch = this.props.modulePath.split('/')[5];
      return (
        <EmptyMessage simple={true}>
          <p>
            <strong>No build history for:</strong>
          </p>
          <span className='crumb'>
            {repo}
          </span>
          <span className='crumb'>
            {branch}
          </span>
          <Link to={this.props.modulePath}>
            {this.props.moduleName}
          </Link>
        </EmptyMessage>
      )
    }

    const {
      build,
      gitInfo,
      module
    } = this.props.modules[0];

    const panelsAreOpen = this.props.modules.length < 5;

    const header = (
      <span className='dashboard-panel__header'>
        <span className='dashboard-panel__header-headline'>
          <Icon for='repo' classNames='icon-muted breadcrumb-icon' />
          <Link className='crumb' to={module.blazarPath.repo}>{gitInfo.repository}</Link> 
          
          <Icon for='branch' classNames='icon-muted breadcrumb-icon' />
          <Link className='crumb' to={module.blazarPath.branch}>{gitInfo.branch}</Link>
          
          <Icon for='module' classNames='icon-muted--extra breadcrumb-icon breadcrumb-icon--thin' />
          <Link to={module.blazarPath.module}> {module.name}</Link>
        </span>
        <span className="dashboard__toggle-container">
        </span>
      </span>
    );

    return (
      <Collapsable
        header={header}
        initialToggleStateOpen={panelsAreOpen} >
          <div className='dashboard-panel__content'>
            <BuildHistoryTable buildHistory={this.props.modules} />
          </div>
          <div className='dashboard-panel__footer'>
            <Link className='dashboard-panel__footer-link' to={module.blazarPath.module}> Full History </Link>
          </div>
        </Collapsable>
    );
  }
}

DashboardStarredModule.propTypes = {
  moduleId: PropTypes.number.isRequired,
  moduleName: PropTypes.string.isRequired,
  modulePath: PropTypes.string.isRequired
};

export default DashboardStarredModule;
