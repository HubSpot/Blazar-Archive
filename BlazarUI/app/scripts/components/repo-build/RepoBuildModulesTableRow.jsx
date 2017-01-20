import React, {Component, PropTypes} from 'react';
import {Link} from 'react-router';
import BuildStates from '../../constants/BuildStates.js';
import moment from 'moment';
import classNames from 'classnames';
import BuildStateIcon from '../shared/BuildStateIcon.jsx';
import SingularityLink from '../branch-state/shared/SingularityLink.jsx';
import {timestampFormatted, timestampDuration, tableRowBuildState, getTableDurationText} from '../Helpers';
import isDebugMode from '../../utils/isDebugMode';


class RepoBuildModulesTableRow extends Component {

  constructor(props, context) {
    super(props, context);

    this.onTableClick = this.onTableClick.bind(this);
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

    if (link === 'build-link') {
      return;
    } else if ([BuildStates.SKIPPED, BuildStates.CANCELLED].indexOf(data.state) === -1) {
      if (!e.metaKey) {
        this.context.router.push(data.blazarPath);
      } else {
        window.open(`${window.config.appRoot}${data.blazarPath}`);
        return;
      }
    }
  }

  renderBuildLink() {
    const {data} = this.props;

    if (data.state === BuildStates.SKIPPED || data.state === BuildStates.CANCELLED) {
      return (
        <span>{data.name}</span>
      );
    }

    return (
      <span>
        <Link className="build-link" to={data.blazarPath}>{data.name}</Link>
      </span>
    );
  }

  renderSingularityLink() {
    const {taskId} = this.props.data;
    if (isDebugMode() && taskId) {
      return (
        <SingularityLink taskId={taskId} />
      );
    }

    return null;
  }

  renderDuration() {
    const {data} = this.props;

    if (data.state === BuildStates.IN_PROGRESS) {
      data.endTimestamp = moment();
    }

    return getTableDurationText(data.state, timestampDuration(data.startTimestamp, data.endTimestamp));
  }

  render() {
    const {data} = this.props;

    return (
      <tr onClick={this.onTableClick} className={this.getRowClassNames(data.state)}>
        <td className="build-status">
          <BuildStateIcon buildState={data.state} />
        </td>
        <td className="table-cell-link">
          {this.renderBuildLink()}
        </td>
        <td>
          {timestampFormatted(data.startTimestamp)}
        </td>
        <td>
          {this.renderDuration()}
        </td>
        <td>
          {this.renderSingularityLink()}
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
