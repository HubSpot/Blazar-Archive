import React, {Component, PropTypes} from 'react';
import TableHead from '../shared/TableHead.jsx';
import ReposTableRow from './ReposTableRow.jsx';

class ReposTable extends Component {

  getRows() {
    const repos = this.props.repos;
    return repos.map((repo, i) =>
      <ReposTableRow
        repo={repos[i]}
        key={i}
        org={this.props.org}
        host={this.props.host}
      />
    );
  }

  render() {
    if (this.props.loading) {
      return <div></div>;
    }

    const columnNames = [
      {label: 'Repository', key: 'name'},
      {label: 'Latest Build', key: 'lastBuild'},
      {label: '', key: 'lastBuildTime'}
    ];

    // TO DO: empty table
    return (
      <table className="table table-hover table-striped">
        <TableHead
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
  org: PropTypes.string.isRequired,
  host: PropTypes.string.isRequired
};

export default ReposTable;
