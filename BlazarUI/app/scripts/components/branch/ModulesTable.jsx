import React, {Component, PropTypes} from 'react';
import TableHead from '../shared/TableHead.jsx';
import ModulesTableRow from './ModulesTableRow.jsx';
import EmptyMessage from '../shared/EmptyMessage.jsx';

class ModulesTable extends Component {

  getRows() {
    const modules = this.props.modules;

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

    if (this.props.modules.length === 0) {
      return (
        <EmptyMessage>
          No build history yet.
        </EmptyMessage>
      )
    }

    const columnNames = [
      {label: 'Module', key: 'module'},
      {label: 'Latest Build', key: 'latestBuild'},
      {label: 'Start Time', key: 'startTime'},
      {label: 'Duration', key: 'duration'},
      {label: 'Commit', key: 'commit'}
    ];

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


ModulesTable.propTypes = {
  loading: PropTypes.bool,
  modules: PropTypes.array.isRequired
};

export default ModulesTable;
