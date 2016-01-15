import React, {Component, PropTypes} from 'react';
import TableMaker from '../shared/TableMaker.jsx';
import BuildHistoryTableRow from './BuildHistoryTableRow.jsx';
import EmptyMessage from '../shared/EmptyMessage.jsx';
import Loader from '../shared/Loader.jsx';

class BuildHistoryTable extends Component {

  render() {
    if (this.props.buildHistory.length === 0) {
      return (
        <EmptyMessage> No build history </EmptyMessage>
      );
    }

    return this.props.buildTable({
      data: this.props.buildHistory,
      columnNames: ['Builds', 'Start Time', 'Duration', 'Commit', 'CommitMessage'],
      rowComponent: BuildHistoryTableRow,
      showProgress: true
    });
  }
}

BuildHistoryTable.propTypes = {
  loading: PropTypes.bool,
  buildHistory: PropTypes.array.isRequired
};

export default TableMaker(BuildHistoryTable, 
  {
    showProgress: true,
    paginate: true
  }
);
