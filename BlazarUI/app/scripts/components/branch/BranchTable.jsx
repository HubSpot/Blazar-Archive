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
      {label: 'Last Build', key: 'lastBuild'},
      {label: 'Branch Modules', key: 'module'}

    ];

    // TO DO: empty table
    return (
      <table className="table table-hover table-striped branch-table">
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
