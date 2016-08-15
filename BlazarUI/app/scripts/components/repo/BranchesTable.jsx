import React, {Component, PropTypes} from 'react';
import TableMaker from '../shared/TableMaker.jsx';
import BranchesTableRow from './BranchesTableRow.jsx';
import EmptyMessage from '../shared/EmptyMessage.jsx';

class BranchesTable extends Component {

  sortMasterFirst() {
    const branches = [];

    this.props.branches.forEach((b) => {
      if (b.gitInfo.branch === 'master') {
        branches.unshift(b);
      } else {
        branches.push(b);
      }
    });

    return branches;
  }

  render() {
    if (this.props.hide) {
      return null;
    } else if (!this.props.branches.length) {
      return (
        <EmptyMessage>No build history</EmptyMessage>
      );
    }

    return this.props.buildTable({
      data: this.sortMasterFirst(),
      columnNames: ['', 'Branch', 'Latest Build', 'Start Time', 'Duration'],
      rowComponent: BranchesTableRow
    });
  }
}

BranchesTable.propTypes = {
  loading: PropTypes.bool,
  hide: PropTypes.bool,
  branches: PropTypes.array.isRequired,
  buildTable: PropTypes.func.isRequired
};

export default TableMaker(BranchesTable, {showProgress: false});
