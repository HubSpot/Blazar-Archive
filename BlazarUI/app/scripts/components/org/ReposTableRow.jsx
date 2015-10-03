import React, {Component, PropTypes} from 'react';
import { Link } from 'react-router';
import Helpers from '../ComponentHelpers';
import {LABELS, iconStatus} from '../constants';
import BuildStates from '../../constants/BuildStates';
import Icon from '../shared/Icon.jsx';
import Sha from '../shared/Sha.jsx';

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

    let sha;
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
  
    if (build.sha !== undefined) {
      const shaBuildInfo = { 
        sha: build.sha 
      };

      const gitInfo = {
        host: repo.host,
        organization: repo.organization,
        repository: repo.repo
      }
      sha = (
        <Sha gitInfo={gitInfo} build={shaBuildInfo} />
      );
    }


    return (
      <tr>
        <td>
          <Icon type='octicon' name='repo' classNames="icon-roomy icon-muted" />
          <Link to={repo.blazarPath.repoBlazarPath}>{repo.repo}</Link>
        </td>
        <td>
          {lastBuildModule}
        </td>
        <td>
          {lastBuildNumber}
        </td>
        <td>
          {lastBuildTimestamp}
        </td>
        <td>
          {sha}
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
