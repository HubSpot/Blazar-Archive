/*global config*/
import React, {Component, PropTypes} from 'react';
import Helpers from '../ComponentHelpers';
import { Link } from 'react-router';
import Icon from '../shared/Icon.jsx';
import Copyable from '../shared/Copyable.jsx';
import {labels, iconStatus} from '../constants';
import ProgressBar from 'react-bootstrap/lib/ProgressBar';
import { contains } from 'underscore';

class BuildHistoryTableRow extends Component {

  getRowClassNames() {
    if (this.props.build.build.state === 'FAILED') {
      return 'bgc-danger';
    }
  }

  getBuildResult(result) {
    let classNames = labels[result];

    return (
      <Icon
        name={iconStatus[result]}
        classNames={classNames}
        title={Helpers.humanizeText(result)}
      />
    );
  }

  render() {
    let {build, gitInfo, module} = this.props.build;

    let commitLink = `https://${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/commit/${build.sha}/`;
    let buildLink = `${config.appRoot}builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${gitInfo.branch}/${module.name + '_' + module.id}/${build.id}`;
    let startTime = Helpers.timestampFormatted(build.startTimestamp);

    let buildNumber = <Link to={buildLink}>{build.buildNumber}</Link>;
    
    let duration = '';
    if (build.startTimestamp !== undefined && build.endTimestamp !== undefined) {
      duration = Helpers.timestampDuration(build.endTimestamp - build.startTimestamp);
    }

    if (contains(['IN_PROGRESS', 'QUEUED', 'LAUNCHING'], build.state)) {
      duration = Helpers.humanizeText(build.state) + '...';
    }

    let sha = '';
    if (build.sha !== undefined) {
      sha = (
        <span>
          <Copyable text={build.sha} hover={this.handleHoverCommit}>
            <Icon type='octicon' classNames='icon-roomy fa-link' name='clippy' />
          </Copyable>
          <a href={commitLink} target="_blank">{Helpers.truncate(build.sha, 8)}</a>
        </span>
        );
    } else {
      sha = 'None';
    }

    let progressBar = '';
    if (this.props.progress) {
      let style = 'default';
      let label = '';
      if (this.props.progress > 100) {
        style = 'warning';
        label = 'Overdue';
      }
      progressBar = <ProgressBar active now={this.props.progress} bsStyle={style} className="build-progress" label={label} />;
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
        <td>
          <div className="progress-container">
            {progressBar}
          </div>
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
      state: PropTypes.oneOf([
        'SUCCEEDED',
        'FAILED',
        'IN_PROGRESS',
        'CANCELLED',
        'QUEUED',
        'LAUNCHING'
      ]),
      startTime: PropTypes.number,
      endTime: PropTypes.number
    }),
    gitInfo: PropTypes.obj,
    module: PropTypes.obj
  }),
  progress: PropTypes.number
};

export default BuildHistoryTableRow;
