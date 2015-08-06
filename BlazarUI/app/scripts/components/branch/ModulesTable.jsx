import React, {Component, PropTypes} from 'react';
import TableHeader from '../shared/TableHeader.jsx';
import ModulesTableRow from './ModulesTableRow.jsx';

class ModulesTable extends Component {

  getRows() {
    let modules = this.props.modules;
    return modules.map((module, i) =>
      <ModulesTableRow
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
      {label: 'Module', key: 'module'},
      {label: 'Latest Build', key: 'latestBuild'},
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


ModulesTable.propTypes = {
  loading: PropTypes.bool,
  modules: PropTypes.array.isRequired
};

export default ModulesTable;
