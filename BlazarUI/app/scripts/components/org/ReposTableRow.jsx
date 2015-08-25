import React, {Component, PropTypes} from 'react';
import Helpers from '../ComponentHelpers';
import {labels, iconStatus} from '../constants';
import Icon from '../shared/Icon.jsx';
import { Link } from 'react-router';

class ReposTableRow extends Component {

  getBuildResult(result) {
    let classNames = labels[result];

    return (
      <Icon
        name={iconStatus[result]}
        classNames={classNames}
      />
    );
  }

  render() {
    let repo = this.props.repo;
    let org = this.props.org;
    let host = this.props.host;
    let repoPath = `/builds/${host}/${org}/${repo.repo}`;

    let lastBuild = (<span></span>);
    if (repo.latestBuild) {
      let build = repo.latestBuild;
      let buildLink = `${window.config.appRoot}/builds/${host}/${org}/${repo.repo}/${build.branch}/${build.module}/${build.buildNumber}`;
      let moduleLink = `${window.config.appRoot}/builds/${host}/${org}/${repo.repo}/${build.branch}/${build.module}`;
      if (build.endTimestamp || build.startTimestamp) {
        lastBuild = (
          <span>
            <Link to={moduleLink}>{build.module}</Link> --- <Link to={buildLink}>#{build.buildNumber}</Link> @ {Helpers.timestampFormatted(build.endTimestamp ? build.endTimestamp : build.startTimestamp)} {this.getBuildResult(build.state)}
          </span>
        );
      } else {
        lastBuild = (
          <span>
            <Link to={moduleLink}>{build.module}</Link> --- <Link to={buildLink}>#{build.buildNumber}</Link>  {this.getBuildResult(build.state)}
          </span>
        );
      }
    }

    return (
      <tr>
        <td>
          <Icon type='octicon' name='repo' classNames="repolist-icon" />
          <Link to={repoPath}>{repo.repo}</Link>
        </td>
        <td>
          {lastBuild}
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
