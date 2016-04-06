import React, {Component, PropTypes} from 'react';
import TableMaker from '../shared/TableMaker.jsx';
import BranchesTableRow from './BranchesTableRow.jsx';
import EmptyMessage from '../shared/EmptyMessage.jsx';

class BranchesTable extends Component {

  sortMasterFirst() {
    let branches = [];

    this.props.branches.map((b) => {
      if (b.gitInfo.branch === "master") {
        branches.unshift(b);
      }

      else {
        branches.push(b);
      }
    });

    return branches;
  }

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
      data: this.sortMasterFirst(),
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
