import React from 'react';
import TableHeader from '../shared/TableHeader.jsx';
import BranchTableRow from './BranchTableRow.jsx';

class BranchTable extends React.Component {

  getRows() {
    let modules = this.props.modules;
    return modules.map((module, i) =>
      <BranchTableRow
        module={modules[i]}
        key={i}
      />
    );
  }

  render() {
    if (this.props.loading) {
      return <div></div>;
    }

    let columnNames = [
      {label: 'Latest Build', key: 'latestBuild'},
      {label: 'Module', key: 'module'},
      {label: 'Start Time', key: 'startTime'},
      {label: 'Duration', key: 'duration'},
      {label: 'Commit', key: 'commit'}
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


BranchTable.propTypes = {
  loading: React.PropTypes.bool,
  modules: React.PropTypes.array.isRequired
};

export default BranchTable;
