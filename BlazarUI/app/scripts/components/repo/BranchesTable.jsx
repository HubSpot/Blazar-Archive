import React, {Component, PropTypes} from 'react';
import TableMaker from '../shared/TableMaker.jsx';
import BranchesTableRow from './BranchesTableRow.jsx';
import EmptyMessage from '../shared/EmptyMessage.jsx';

class BranchesTable extends Component {

  render() {
    if (this.props.modules.length === 0) {
      return (
        <EmptyMessage> No build history </EmptyMessage>
      );
    }

    const columnNames = [
      {label: 'Branch', key: 'branch'},
      {label: 'Module', key: 'module'},
      {label: 'Latest Build', key: 'latestBuild'},
      {label: 'Start Time', key: 'startTime'},
      {label: 'Duration', key: 'duration'},
      {label: 'Commit', key: 'commit'}
    ];

    return this.props.buildTable({
      data: this.props.modules,
      columnNames: columnNames,
      rowComponent: BranchesTableRow
    });
  }
}

BranchesTable.propTypes = {
  loading: PropTypes.bool,
  modules: PropTypes.array.isRequired
};

export default TableMaker(BranchesTable, {showProgress: false});
