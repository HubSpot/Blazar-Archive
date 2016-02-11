import React, {Component, PropTypes} from 'react';
import BuildStates from '../../constants/BuildStates.js';
import { Link } from 'react-router';
import {LABELS, iconStatus} from '../constants';

import {tableRowBuildState, timestampFormatted, humanizeText, buildResultIcon} from '../Helpers';

import Icon from '../shared/Icon.jsx';
import Sha from '../shared/Sha.jsx';


class BranchBuildHistoryTableRow extends Component {

  
  renderSha() {
    const {data, params} = this.props;
    let sha;

    if (data.sha !== undefined) {
      const gitInfo = {
        host: params.host,
        organization: params.org,
        repository: params.repo,
      }

      return (
        <Sha gitInfo={gitInfo} build={data} />  
      );
    }  
  }
  
  renderDuration() {
    const {data} = this.props;

    let durationText = data.duration;

    if (data.state === BuildStates.IN_PROGRESS) {
      durationText = 'In Progress';
    }

    else if (data.state === BuildStates.SKIPPED) {
      durationText = 'Skipped';
    }

    else if (data.state === BuildStates.QUEUED) {
      durationText = 'Queued';
    }

    else if (data.state === BuildStates.LAUNCHING) {
      durationText = 'Launching';
    }

    return durationText;
  }
  
  renderStartTime() {
    return timestampFormatted(this.props.data.startTimestamp);
  }

  renderBuildLink() {
    const {data} = this.props;
    
    if (data.state === BuildStates.LAUNCHING || data.state === BuildStates.QUEUED) {
      return data.buildNumber;
    }

    return (
      <Link to={data.blazarPath}>{data.buildNumber}</Link>
    );
  }

  render() {
    const {data, params} = this.props;

    return (
      <tr className={tableRowBuildState(data.state)}>
        <td className='build-status'>
          {buildResultIcon(data.state)}
        </td>
        <td className='build-result-link'>
          <span>{this.renderBuildLink()}</span>
        </td>
        <td>
          {this.renderStartTime()}
        </td>
        <td>
          {this.renderDuration()}
        </td>
        <td>
          {this.renderSha()}
        </td>
      </tr>
    );
  }
}

BranchBuildHistoryTableRow.propTypes = {
  data: PropTypes.object.isRequired
};

export default BranchBuildHistoryTableRow;
