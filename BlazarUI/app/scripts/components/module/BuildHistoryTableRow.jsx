/*global app*/
import React from 'react';
import Helpers from '../ComponentHelpers';
import { Link } from 'react-router';
import Icon from '../shared/Icon.jsx';
import Copyable from '../shared/Copyable.jsx';
import {labels, iconStatus} from '../constants';

class BuildHistoryTableRow extends React.Component {

  handleHoverCommit() {
    console.log('hover');
  }

  handleCopyCommit() {
    console.log('copy');
  }

  getRowClassNames() {
    if (this.props.build.buildState.result === 'FAILED') {
      return 'bgc-danger';
    }
  }

  getBuildResult() {
    let result = this.props.build.buildState.result;
    let classNames = labels[result];

    return (
      <Icon
        name={iconStatus[result]}
        classNames={classNames}
      />
    );
  }

  render() {
    let {buildState, gitInfo, module} = this.props.build;

    let commitLink = `https://${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/commit/${buildState.commitSha}/`;
    let buildLink = `${app.config.appRoot}/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${gitInfo.branch}/${module.name}/${buildState.buildNumber}`;
    let startTime = Helpers.timestampFormatted(buildState.startTime);
    let duration = buildState.duration;
    let buildNumber = <Link to={buildLink}>{buildState.buildNumber}</Link>;
    let sha = Helpers.truncate(buildState.commitSha, 8);

    if (buildState.result === 'IN_PROGRESS') {
      duration = 'In Progress...';
    }

    return (
      <tr className={this.getRowClassNames()}>
        <td className='build-status'>
          {this.getBuildResult()}
        </td>
        <td>
          {buildNumber}
        </td>
        <td>
          {startTime}
        </td>
        <td>
          {duration}
        </td>
        <td>
          <Copyable text={buildState.commitSha} click={this.handleCopyCommit} hover={this.handleHoverCommit}>
            <Icon classNames='icon-roomy fa-link' name='clipboard' />
          </Copyable>
          <a href={commitLink} target="_blank">{sha}</a>
        </td>
      </tr>
    );
  }
}



BuildHistoryTableRow.propTypes = {
  build: React.PropTypes.shape({
    buildState: React.PropTypes.shape({
      buildNumber: React.PropTypes.number,
      commitSha: React.PropTypes.string,
      result: React.PropTypes.oneOf(['SUCCEEDED', 'FAILED', 'IN_PROGRESS', 'CANCELLED']),
      startTime: React.PropTypes.number,
      endTime: React.PropTypes.number
    }),
    gitInfo: React.PropTypes.obj,
    module: React.PropTypes.obj
  })
};




export default BuildHistoryTableRow;
