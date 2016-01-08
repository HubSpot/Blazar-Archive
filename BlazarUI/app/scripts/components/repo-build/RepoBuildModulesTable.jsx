import React, {Component, PropTypes} from 'react';
import TableMaker from '../shared/TableMaker.jsx';
import RepoBuildModulesTableRow from './RepoBuildModulesTableRow.jsx';
import EmptyMessage from '../shared/EmptyMessage.jsx';
import Loader from '../shared/Loader.jsx';

class RepoBuildModulesTable extends Component {

  render() {
    if (this.props.loading) {
      return null;
    }

    if (this.props.data.length === 0) {
      return (
        <EmptyMessage> No build history </EmptyMessage>
      );
    }

    const columnNames = [
      {label: '', key: 'state'},
      {label: 'Module', key: 'module'},
      {label: 'Build', key: 'buildNumber'},
      {label: 'Start Time', key: 'startTime'},
      {label: 'Duration', key: 'duration'},
      {label: 'Singularity Task', key: 'taskid'},
      {label: '', key: 'progress'}
    ];

    return this.props.buildTable({
      data: this.props.data,
      columnNames: columnNames,
      rowComponent: RepoBuildModulesTableRow,
      showProgress: true,
      params: this.props.params
    });
  }
}

RepoBuildModulesTable.propTypes = {
  loading: PropTypes.bool,
  data: PropTypes.array.isRequired
};

export default TableMaker(RepoBuildModulesTable, 
  {
    showProgress: true,
    paginate: true
  }
);
