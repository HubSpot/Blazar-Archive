import React, {Component, PropTypes} from 'react';
import BuildStates from '../../constants/BuildStates.js';
import {Link} from 'react-router';
import {has} from 'underscore';
import {tableRowBuildState, timestampFormatted, buildResultIcon, timestampDuration} from '../Helpers';
import moment from 'moment';
import classNames from 'classnames';

class BranchesTableRow extends Component {

  constructor(props, context) {
    super(props, context);

    this.state = {
      moment: moment()
    };
  }

  componentDidMount() {
    this.interval = setInterval(this.updateMoment.bind(this), 1000);
  }

  componentWillUnmount() {
    clearInterval(this.interval);
  }

  updateMoment() {
    this.setState({
      moment: moment()
    });
  }

  getRowClassNames(state) {
    return classNames([
      tableRowBuildState(state),
      'clickable-table-row'
    ]);
  }

  onTableClick(blazarPath, e) {
    const link = e.target.className;

    if (!blazarPath || link === 'branch-link' || link === 'build-link' || link === 'sha-link') {
      return;
    } else if (!e.metaKey) {
      this.context.router.push(blazarPath);
    } else {
      window.open(`${window.config.appRoot}${blazarPath}`);
      return;
    }
  }

  renderBranchLink() {
    const {gitInfo} = this.props.data;

    return (
      <span>
        <Link className="branch-link" to={gitInfo.blazarBranchPath}>{gitInfo.branch}</Link>
      </span>
    );
  }

  renderNoHistoryTable() {
    return (
      <tr>
        <td></td>
        <td>
          {this.renderBranchLink()}
        </td>
        <td>No History</td>
        <td></td>
        <td></td>
        <td></td>
      </tr>
    );
  }

  renderFullTable() {
    const {
      lastBuild,
      inProgressBuild,
      pendingBuild
    } = this.props.data;

    let buildLink;
    let build;

    if (inProgressBuild) {
      build = inProgressBuild;
    } else if (pendingBuild) {
      build = pendingBuild;
    } else {
      build = lastBuild;
    }

    let duration = build.duration;

    if (build.state === BuildStates.IN_PROGRESS) {
      duration = timestampDuration(build.startTimestamp, this.state.moment);
    }

    if (build.blazarPath) {
      buildLink = (
        <Link className="build-link" to={build.blazarPath}>
          {build.buildNumber}
        </Link>
      );
    }

    return (
      <tr onClick={(e) => this.onTableClick(build.blazarPath, e)} className={this.getRowClassNames(build.state)}>
        <td className="build-status">
          {buildResultIcon(build.state)}
        </td>
        <td>
          {this.renderBranchLink()}
        </td>
        <td className="build-result-link">
          {buildLink}
        </td>
        <td>
          {timestampFormatted(build.startTimestamp)}
        </td>
        <td>
          {duration}
        </td>
      </tr>
    );
  }

  render() {
    if (has(this.props.data, 'lastBuild') || has(this.props.data, 'inProgressBuild') || has(this.props.data, 'pendingBuild')) {
      return this.renderFullTable();
    }

    return this.renderNoHistoryTable();
  }

}

BranchesTableRow.contextTypes = {
  router: PropTypes.object.isRequired
};

BranchesTableRow.propTypes = {
  data: PropTypes.object.isRequired
};

export default BranchesTableRow;
