import React, {Component, PropTypes} from 'react';
import Helpers from '../ComponentHelpers';
import {labels, iconStatus} from '../constants';
import Icon from '../shared/Icon.jsx';
import { Link } from 'react-router';

class ReposTableRow extends Component {

  getBuildResult(result) {
    let classNames = labels[result] + ' icon-roomy';

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

    let repoPath = `/builds/${host}/${org}/${repo.repo}`;

    if (repo.latestBuild) {
      let build = repo.latestBuild;
      let buildLink = `${window.config.appRoot}/builds/${host}/${org}/${repo.repo}/${build.branch}/${build.module}/${build.buildNumber}`;
      let moduleLink = `${window.config.appRoot}/builds/${host}/${org}/${repo.repo}/${build.branch}/${build.module}`;

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
          <Link to={repoPath}>{repo.repo}</Link>
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
