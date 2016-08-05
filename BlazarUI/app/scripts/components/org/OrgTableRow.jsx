import React, {Component, PropTypes} from 'react';
import {Link} from 'react-router';
import classNames from 'classnames';

import {tableRowBuildState, timestampFormatted, buildResultIcon} from '../Helpers';

class OrgTableRow extends Component {

  getRowClassNames(state) {
    return classNames([
      tableRowBuildState(state),
      'clickable-table-row'
    ]);
  }

  onTableClick(blazarPath, e) {
    const link = e.target.className;

    if (!blazarPath || link === 'repo-link' || link === 'build-link' || link === 'sha-link') {
      return;
    } else if (!e.metaKey) {
      this.context.router.push(blazarPath);
    } else {
      window.open(`${window.config.appRoot}${blazarPath}`);
      return;
    }
  }

  render() {
    const build = this.props.data.get('lastBuild').toJS();

    let buildLink;

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
          <Link className="repo-link" to={this.props.data.get('blazarRepositoryPath')}>
            {this.props.data.get('repository')}
          </Link>
        </td>
        <td>
          {buildLink}
        </td>
        <td>
          {timestampFormatted(build.startTimestamp)}
        </td>
        <td>
          {build.duration}
        </td>
      </tr>
    );
  }
}

OrgTableRow.contextTypes = {
  router: PropTypes.object.isRequired
};

OrgTableRow.propTypes = {
  data: PropTypes.object.isRequired
};

export default OrgTableRow;
