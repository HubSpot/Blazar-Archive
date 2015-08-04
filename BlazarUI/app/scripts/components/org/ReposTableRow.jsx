import React, {Component, PropTypes} from 'react';
//import { Link } from 'react-router';

class ReposTableRow extends Component {

  render() {
    let repo = this.props.repo;
    let org = this.props.org;
    let repoPath = org + "/" + repo;
    return (
      <tr>
        <td>
          <a href={repoPath}>{repo}</a>
        </td>
      </tr>
    );
  }
}

ReposTableRow.propTypes = {
  repo: PropTypes.string.isRequired,
  org: PropTypes.string.isRequired
};

export default ReposTableRow;
