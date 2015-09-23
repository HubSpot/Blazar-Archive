/*global config*/
import React, {Component, PropTypes} from 'react';
import BuildStates from '../../constants/BuildStates.js';
import ProgressBar from 'react-bootstrap/lib/ProgressBar';
import { contains } from 'underscore';
import Helpers from '../ComponentHelpers';
import {LABELS, iconStatus} from '../constants';
import { Link } from 'react-router';
import Icon from '../shared/Icon.jsx';
import Sha from '../shared/Sha.jsx';

class BuildHistoryTableRow extends Component {

  getRowClassNames() {
    if (this.props.build.build.state === BuildStates.FAILED) {
      return 'bgc-danger';
    }
  }

  getBuildResult(result) {
    const classNames = LABELS[result];

    return (
      <Icon
        name={iconStatus[result]}
        classNames={classNames}
        title={Helpers.humanizeText(result)}
      />
    );
  }

  render() {
    const {
      build, 
      gitInfo, 
      module
    } = this.props.build;

    const buildLink = `${config.appRoot}/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${gitInfo.branch}/${module.name}/${build.buildNumber}`;
    const startTime = Helpers.timestampFormatted(build.startTimestamp);
    const buildNumber = <Link to={buildLink}>{build.buildNumber}</Link>;
    let sha, duration;

    if (build.startTimestamp !== undefined && build.endTimestamp !== undefined) {
      duration = Helpers.timestampDuration(build.endTimestamp - build.startTimestamp);
    }

    if (contains([BuildStates.IN_PROGRESS, BuildStates.QUEUED, BuildStates.LAUNCHING], build.state)) {
      duration = Helpers.humanizeText(build.state) + '...';
    }

    if (build.sha !== undefined) {
      sha = <Sha gitInfo={gitInfo} build={build} />;
    }

    let progressBar;
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
