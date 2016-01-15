import React, {Component, PropTypes} from 'react';
import TableMaker from '../shared/TableMaker.jsx';
import BranchesTableRow from './BranchesTableRow.jsx';
import EmptyMessage from '../shared/EmptyMessage.jsx';

class BranchesTable extends Component {

  render() {
    if (this.props.hide) {
      return null;
    }

    if (this.props.branches.length === 0) {
      return (
        <EmptyMessage> No build history </EmptyMessage>
      );
    }

    return this.props.buildTable({
      data: this.props.branches,
      columnNames: ['', 'Branch', 'Latest Build', 'Start Time', 'Duration', 'Commit'],
      rowComponent: BranchesTableRow
    });
  }
}

BranchesTable.propTypes = {
  loading: PropTypes.bool,
  branches: PropTypes.array.isRequired
};

export default TableMaker(BranchesTable, {showProgress: false});
