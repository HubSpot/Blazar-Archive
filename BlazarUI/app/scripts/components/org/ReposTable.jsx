import React, {Component, PropTypes} from 'react';
import TableHeader from '../shared/TableHeader.jsx';
import ReposTableRow from './ReposTableRow.jsx';

class ReposTable extends Component {

  getRows() {
    let repos = this.props.repos;
    return repos.map((repo, i) =>
      <ReposTableRow
        repo={repos[i]}
        key={i}
        org={this.props.org}
      />
    );
  }

  render() {
    if (this.props.loading) {
      return <div></div>;
    }

    let columnNames = [
      {label: 'Name', key: 'name'},
      {label: 'Last Build', key: 'lastBuild'}
    ];

    // TO DO: empty table
    return (
      <table className="table table-hover table-striped">
        <TableHeader
          columnNames={columnNames}
        />
        <tbody>
          {this.getRows()}
        </tbody>
      </table>

    );
  }

}


ReposTable.propTypes = {
  loading: PropTypes.bool,
  repos: PropTypes.array.isRequired,
  org: PropTypes.string.isRequired
};

export default ReposTable;
