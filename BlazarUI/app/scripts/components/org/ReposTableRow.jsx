import React, {Component, PropTypes} from 'react';
import Helpers from '../ComponentHelpers';
import {labels, iconStatus} from '../constants';
import Icon from '../shared/Icon.jsx';
//import { Link } from 'react-router';

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
    let repoPath = org + "/" + repo.repo;

    let lastBuild = (<span></span>);
    if(repo.latestBuild) {
      let build = repo.latestBuild;
      let buildLink = `${org}/${repo.repo}/${build.branch}/${build.module}_${build.moduleId}/${build.id}`;
      let moduleLink = `${org}/${repo.repo}/${build.branch}/${build.module}_${build.moduleId}`;
      lastBuild = (
        <span>
          <a href={moduleLink}>{build.module}</a> --- <a href={buildLink}>#{build.number}</a> @ {Helpers.timestampFormatted(build.endTimestamp)} {this.getBuildResult(build.state)}
        </span>
      );
    }

    return (
      <tr>
        <td>
          <Icon type='octicon' name='repo' classNames="repolist-icon" />
          <a href={repoPath}>{repo.repo}</a>
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
  org: PropTypes.string.isRequired
};

export default ReposTableRow;
