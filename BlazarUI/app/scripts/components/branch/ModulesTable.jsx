import React, {Component, PropTypes} from 'react';
import TableMaker from '../shared/TableMaker.jsx';
import ModulesTableRow from './ModulesTableRow.jsx';
import EmptyMessage from '../shared/EmptyMessage.jsx';

class ModulesTable extends Component {

  render() {
    if (this.props.modules.length === 0) {
      return (
        <EmptyMessage> No build history </EmptyMessage>
      );
    }

    const columnNames = [
      {label: 'Module', key: 'module'},
      {label: 'Latest Build', key: 'latestBuild'},
      {label: 'Start Time', key: 'startTime'},
      {label: 'Duration', key: 'duration'},
      {label: 'Commit', key: 'commit'}
    ];

    return this.props.buildTable({
      data: this.props.modules,
      columnNames: columnNames,
      rowComponent: ModulesTableRow
    });
  }
}

ModulesTable.propTypes = {
  loading: PropTypes.bool,
  modules: PropTypes.array.isRequired
};

export default TableMaker(ModulesTable, {showProgress: false});
