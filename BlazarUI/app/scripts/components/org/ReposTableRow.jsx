import React, {Component, PropTypes} from 'react';
import Helpers from '../ComponentHelpers';
//import { Link } from 'react-router';

class ReposTableRow extends Component {

  render() {
    let repo = this.props.repo;
    let org = this.props.org;
    let repoPath = org + "/" + repo.repo;

    let lastBuild = (<span>None</span>);
    if(repo.latestBuild) {
      let build = repo.latestBuild;
      lastBuild = (
        <span>
          {build.module} #{build.number} at {Helpers.timestampFormatted(build.endTimestamp)} ({Helpers.humanizeText(build.state)})
        </span>
      );
    }

    console.log(repo);
    return (
      <tr>
        <td>
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
