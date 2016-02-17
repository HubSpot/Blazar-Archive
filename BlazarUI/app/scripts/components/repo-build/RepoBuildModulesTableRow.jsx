import React, {Component, PropTypes} from 'react';
import {Link} from 'react-router';
import BuildStates from '../../constants/BuildStates.js';
import ProgressBar from 'react-bootstrap/lib/ProgressBar';
import {contains, has} from 'underscore';
import {humanizeText, timestampFormatted, timestampDuration, tableRowBuildState, truncate, buildResultIcon, getTableDurationText} from '../Helpers';

class RepoBuildModulesTableRow extends Component {
  
  renderBuildLink() {
    const {data, params} = this.props;

    if (data.state === BuildStates.SKIPPED || data.state === BuildStates.CANCELLED) {
      return (
        <span>{data.name}</span>
      );
    }

    return (
      <span><Link to={data.blazarPath}>{data.name}</Link></span>
    );    
  }

  isDebugMode() {
    return window.location.href.indexOf('?debug') > -1;
  }

  renderSingularityLink() {
    if (!this.isDebugMode()) {
      return null;
    }

    const {taskId} = this.props.data;

    if (!taskId) {
      return null;
    }

    //
    // to do: surface singularity env
    //
    const singularityPath = `https://tools.hubteamqa.com/singularity/task/${taskId}`;

    return (
      <td>
        <a href={singularityPath} target="_blank">{truncate(taskId, 30, true)}</a>
      </td>
    );
  }

  renderDuration() {
    const {data} = this.props;
    
    return getTableDurationText(data.state, timestampDuration(data.startTimestamp, data.endTimestamp));
  }

  render() {
    const {data, params} = this.props;
    return (
      <tr className={tableRowBuildState(data.state)}>
        <td className='build-status'>
          {buildResultIcon(data.state)}
        </td>
        <td className='table-cell-link'>
          {this.renderBuildLink()}
        </td>
        <td>
          {timestampFormatted(data.startTimestamp)}
        </td>
        <td>
          {this.renderDuration()}
        </td>
        {this.renderSingularityLink()}
        <td>
        </td>
      </tr>
    );
  }

}

RepoBuildModulesTableRow.propTypes = {
  data: PropTypes.object.isRequired
};

export default RepoBuildModulesTableRow;
