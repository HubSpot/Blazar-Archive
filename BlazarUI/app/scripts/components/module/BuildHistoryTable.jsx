import React, {Component, PropTypes} from 'react';
import TableMaker from '../shared/TableMaker.jsx';
import BuildHistoryTableRow from './BuildHistoryTableRow.jsx';
import EmptyMessage from '../shared/EmptyMessage.jsx';
import Loader from '../shared/Loader.jsx';

class BuildHistoryTable extends Component {

  render() {
    
    if (this.props.loading) {
      return (
        <Loader align='top-center' />
      );
    }

    if (this.props.buildHistory.length === 0) {
      return (
        <EmptyMessage> No build history </EmptyMessage>
      );
    }

    const columnNames = [
      {label: '', key: 'result'},
      {label: 'Build', key: 'buildNumber'},
      {label: 'Start Time', key: 'startTime'},
      {label: 'Duration', key: 'duration'},
      {label: 'Commit', key: 'commit'},
      {label: 'Commit Message', key: 'commitMessage'},
      {label: '', key: 'progress'}
    ];

    return this.props.buildTable({
      data: this.props.buildHistory,
      columnNames: columnNames,
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
