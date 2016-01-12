/*global config*/
import React, {Component, PropTypes} from 'react';
import {Link} from 'react-router';
import BuildStates from '../../constants/BuildStates.js';
import ProgressBar from 'react-bootstrap/lib/ProgressBar';
import {contains, has} from 'underscore';
import {humanizeText, timestampFormatted, timestampDuration, tableRowBuildState, truncate, renderBuildStatusIcon} from '../Helpers';

class RepoBuildModulesTableRow extends Component {
  
  renderBuildLink() {
    const {data, params} = this.props;

    // To do: dont pull in global appRoot
    const buildLink = `${config.appRoot}/builds/${params.host}/${params.org}/${params.repo}/${params.branch}/${params.repoBuildId}/${data.name}`;    

    return (
      <Link to={buildLink}>{data.name}</Link>   
    );    
  }

  renderSingularityLink() {
    const {taskId} = this.props.data;

    if (!taskId) {
      return null;
    }

    // to do: surface singularity env
    const singularityPath = `https://tools.hubteamqa.com/singularity/task/${taskId}`;

    return (
      <a href={singularityPath} target="_blank">{truncate(taskId, 30, true)}</a>  
    );
  }

  render() {
    const {data, params} = this.props;
    
    return (
      <tr className={tableRowBuildState(data.state)}>
        <td className='build-status'>
          {renderBuildStatusIcon(this.props.data)}
        </td>
        <td className='table-cell-link'>
          {this.renderBuildLink()}
        </td>
        <td>
          {timestampFormatted(data.startTimestamp)}
        </td>
        <td>
          {data.state === BuildStates.IN_PROGRESS ? 'In Progress' : timestampDuration(data.startTimestamp, data.endTimestamp)}
        </td>
        <td>
          {this.renderSingularityLink()}
        </td>
        <td>
        </td>
      </tr>
    );
  }

}

// RepoBuildModulesTableRow.propTypes = {
// };

export default RepoBuildModulesTableRow;
