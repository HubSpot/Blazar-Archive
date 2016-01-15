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

    return this.props.buildTable({
      data: this.props.data.toJS(),
      columnNames: ['Build', 'Start Time', 'Duration', 'Commit'],
      rowComponent: BranchBuildHistoryTableRow,
      params: this.props.params
    });
  }
}

BranchBuildHistoryTable.propTypes = {
  loading: PropTypes.bool,
  data: PropTypes.object.isRequired
};

export default TableMaker(BranchBuildHistoryTable, {showProgress: false});
