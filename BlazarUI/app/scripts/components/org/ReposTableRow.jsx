import React, {Component, PropTypes} from 'react';
import Helpers from '../ComponentHelpers';
import {labels, iconStatus} from '../constants';
import Icon from '../shared/Icon.jsx';
import { Link } from 'react-router';

class ReposTableRow extends Component {

  getBuildResult(result) {
    const classNames = labels[result];

    return (
      <Icon
        name={iconStatus[result]}
        classNames={classNames}
      />
    );
  }

  render() {
    const repo = this.props.repo;
    let lastBuild = null;

    if (repo.latestBuild) {
      const build = repo.latestBuild;
      lastBuild = (
        <span>
          <Link to={repo.moduleLink}>{build.module}</Link> --- <Link to={repo.latestBuildLink}>#{build.number}</Link> @ {Helpers.timestampFormatted(build.endTimestamp)} {this.getBuildResult(build.state)}
        </span>
      );
    }

    return (
      <tr>
        <td>
          <Icon type='octicon' name='repo' classNames="repolist-icon" />
          <Link to={repo.repoLink}>{repo.name}</Link>
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
