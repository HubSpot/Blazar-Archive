/*global config*/
import React, {Component, PropTypes} from 'react';
import Helpers from '../ComponentHelpers';
import { Link } from 'react-router';
import Icon from '../shared/Icon.jsx';
import Copyable from '../shared/Copyable.jsx';
import {labels, iconStatus} from '../constants';

class BuildHistoryTableRow extends Component {

  handleHoverCommit() {
    console.log('hover');
  }

  handleCopyCommit() {
    console.log('copy');
  }

  getRowClassNames() {
    if (this.props.build.state === 'FAILED') {
      return 'bgc-danger';
    }
  }

  getBuildResult(result) {
    let classNames = labels[result];

    return (
      <Icon
        name={iconStatus[result]}
        classNames={classNames}
      />
    );
  }

  render() {
    let {build, gitInfo, module} = this.props.build;
    console.log(this.props.build);

    let commitLink = `https://${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/commit/${build.sha}/`;
    let buildLink = `${config.appRoot}/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${gitInfo.branch}/${module.name}/${build.buildNumber}`;
    let startTime = Helpers.timestampFormatted(build.startTimestamp);

    let duration = '';
    if (build.startTimestamp !== undefined && build.endTimestamp !== undefined) {
      duration = Helpers.timestampDuration(build.endTimestamp - build.startTimestamp);
    } else if (build.startTimestamp !== undefined) {
      duration = 'In Progress...';
    } else {
      duration = '';
    }

    let buildNumber = <Link to={buildLink}>{build.buildNumber}</Link>;
    let sha = '';

    if (build.state === 'IN_PROGRESS') {
      duration = 'In Progress...';
    }

    if (build.sha !== undefined) {
      sha = (<span><Copyable text={build.sha} click={this.handleCopyCommit} hover={this.handleHoverCommit}>
              <Icon classNames='icon-roomy fa-link' name='clipboard' />
            </Copyable>
            <a href={commitLink} target="_blank">{Helpers.truncate(build.sha, 8)}</a></span>);
    } else {
      sha = 'None';
    }

    return (
      <tr className={this.getRowClassNames()}>
        <td className='build-status'>
          {this.getBuildResult(build.state)}
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
          {sha}
        </td>
      </tr>
    );
  }
}



BuildHistoryTableRow.propTypes = {
  build: PropTypes.shape({
    build: PropTypes.shape({
      buildNumber: PropTypes.number,
      commitSha: PropTypes.string,
      result: PropTypes.oneOf(['SUCCEEDED', 'FAILED', 'IN_PROGRESS', 'CANCELLED']),
      startTime: PropTypes.number,
      endTime: PropTypes.number
    }),
    gitInfo: PropTypes.obj,
    module: PropTypes.obj
  })
};




export default BuildHistoryTableRow;
