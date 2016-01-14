import React, {Component, PropTypes} from 'react';
import TableMaker from '../shared/TableMaker.jsx';
import BranchBuildHistoryTableRow from './BranchBuildHistoryTableRow.jsx';
import EmptyMessage from '../shared/EmptyMessage.jsx';
import Loader from '../shared/Loader.jsx';

class BranchBuildHistoryTable extends Component {

  render() {
    if (!this.props.data) {
      return (
        <Loader align='top-center' />
      );
    }

    if (this.props.data.size === 0) {
      return (
        <EmptyMessage> No build history </EmptyMessage>
      );
    }

    const columnNames = [
      {label: '', key: 'state'},
      {label: 'Build', key: 'build'},
      {label: 'Start Time', key: 'startTime'},
      {label: 'Duration', key: 'duration'},
      {label: 'Commit', key: 'commit'}
    ];

    return this.props.buildTable({
      data: this.props.data.toJS(),
      columnNames: columnNames,
      rowComponent: BranchBuildHistoryTableRow
    });
  }
}

BranchBuildHistoryTable.propTypes = {
  loading: PropTypes.bool,
  data: PropTypes.object.isRequired
};

export default TableMaker(BranchBuildHistoryTable, {showProgress: false});
