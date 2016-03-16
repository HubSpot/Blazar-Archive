import React, {Component, PropTypes} from 'react';
import {Link} from 'react-router';
import BuildStates from '../../constants/BuildStates.js';
import ProgressBar from 'react-bootstrap/lib/ProgressBar';
import {contains, has} from 'underscore';
import moment from 'moment';
import classNames from 'classnames';
import {humanizeText, timestampFormatted, timestampDuration, tableRowBuildState, truncate, buildResultIcon, getTableDurationText} from '../Helpers';

class RepoBuildModulesTableRow extends Component {

  constructor(props, context) {
    super(props, context);
  }

  isDebugMode() {
    return window.location.href.indexOf('?debug') > -1;
  }

  getRowClassNames(state) {
    if ([BuildStates.SKIPPED, BuildStates.CANCELLED].indexOf(state) > -1) {
      return tableRowBuildState(state);
    }

    return classNames([
      tableRowBuildState(state),
      'clickable-table-row'
    ]);
  }

  onTableClick(e) {
    const {data} = this.props;
    const link = e.target.className;

    if (link === 'build-link' || link === 'singularity-link') {
      return;
    }

    else if ([BuildStates.SKIPPED, BuildStates.CANCELLED].indexOf(data.state) === -1) {
      this.context.router.push(data.blazarPath);
    }
  }

  renderBuildLink() {
    const {data, params} = this.props;

    if (data.state === BuildStates.SKIPPED || data.state === BuildStates.CANCELLED) {
      return (
        <span>{data.name}</span>
      );
    }

    return (
      <span><Link className='build-link' to={data.blazarPath}>{data.name}</Link></span>
    );    
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
        <a className='singularity-link' href={singularityPath} target="_blank">{truncate(taskId, 30, true)}</a>
      </td>
    );
  }

  renderDuration() {
    const {data} = this.props;

    if (data.state === BuildStates.IN_PROGRESS) {
      data.endTimestamp = moment();
    }
    
    return getTableDurationText(data.state, timestampDuration(data.startTimestamp, data.endTimestamp));
  }

  render() {
    const {data, params} = this.props;
    return (
      <tr onClick={this.onTableClick.bind(this)}className={this.getRowClassNames(data.state)}>
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

RepoBuildModulesTableRow.contextTypes = {
  router: PropTypes.object.isRequired
};

RepoBuildModulesTableRow.propTypes = {
  data: PropTypes.object.isRequired
};

export default RepoBuildModulesTableRow;
