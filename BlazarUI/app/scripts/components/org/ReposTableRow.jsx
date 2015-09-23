import React, {Component, PropTypes} from 'react';
import Helpers from '../ComponentHelpers';
import {LABELS, iconStatus} from '../constants';
import Icon from '../shared/Icon.jsx';
import { Link } from 'react-router';

class ReposTableRow extends Component {

  getBuildResult(result) {
    const classNames = LABELS[result] + ' icon-roomy';

    return (
      <Icon
        name={iconStatus[result]}
        classNames={classNames}
      />
    );
  }

  render() {
    let lastBuildNumber, lastBuildModule, lastBuildTimestamp;
    
    const {
      repo,
      org,
      host
    } = this.props;

    if (repo.latestBuild) {
      const build = repo.latestBuild;
      // to do: move link creation to collection
      const buildLink = `${window.config.appRoot}/builds/${host}/${org}/${repo.repo}/${build.branch}/${build.module}/${build.buildNumber}`;
      const moduleLink = `${window.config.appRoot}/builds/${host}/${org}/${repo.repo}/${build.branch}/${build.module}`;

      if (build.endTimestamp || build.startTimestamp) {
        lastBuildTimestamp = Helpers.timestampFormatted(build.endTimestamp ? build.endTimestamp : build.startTimestamp);
      }

      lastBuildNumber = (
          <span>
            {this.getBuildResult(build.state)}
            <Link to={buildLink}>#{build.buildNumber}</Link>
          </span>
      );

      lastBuildModule = (
          <Link to={moduleLink}>{build.module}</Link>
      );

    }

    return (
      <tr>
        <td>
          <Icon type='octicon' name='repo' classNames="icon-roomy icon-muted" />
          <Link to={repo.blazarPath.repoBlazarPath}>{repo.repo}</Link>
        </td>
        <td>
          {lastBuildNumber}
          {' '}
          {lastBuildModule}
        </td>
        <td>
          {lastBuildTimestamp}
        </td>
      </tr>
    );
  }
}

ReposTableRow.propTypes = {
  repo: PropTypes.object.isRequired,
  org: PropTypes.string.isRequired,
  host: PropTypes.string.isRequired
};

export default ReposTableRow;
