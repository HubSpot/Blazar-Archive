import React, {Component, PropTypes} from 'react';
import TableMaker from '../shared/TableMaker.jsx';
import RepoBuildModulesTableRow from './RepoBuildModulesTableRow.jsx';
import EmptyMessage from '../shared/EmptyMessage.jsx';

class RepoBuildModulesTable extends Component {

  sortModules() {
    if (!this.props.currentRepoBuild || !this.props.currentRepoBuild.dependencyGraph || !this.props.currentRepoBuild.dependencyGraph.topologicalSort) {
      return this.props.data;
    }

    const topologicalSort = this.props.currentRepoBuild.dependencyGraph.topologicalSort;

    return this.props.data.sort((a, b) => {
      const indexA = topologicalSort.indexOf(a.moduleId);
      const indexB = topologicalSort.indexOf(b.moduleId);

      if (indexA < indexB) {
        return -1;
      }

      return 1;
    });
  }

  render() {
    if (this.props.loading) {
      return null;
    }

    if (this.props.data.length === 0) {
      return (
        <EmptyMessage> There's no information available for this build right now. </EmptyMessage>
      );
    }

    return this.props.buildTable({
      data: this.sortModules(),
      columnNames: ['', 'Module Build', 'Start Time', 'Duration', ''],
      rowComponent: RepoBuildModulesTableRow,
      showProgress: true,
      params: this.props.params
    });
  }
}

RepoBuildModulesTable.propTypes = {
  loading: PropTypes.bool,
  data: PropTypes.array.isRequired,
  currentRepoBuild: PropTypes.object.isRequired,
  buildTable: PropTypes.func.isRequired,
  params: PropTypes.object.isRequired
};

export default TableMaker(RepoBuildModulesTable,
  {
    showProgress: true,
    paginate: true
  }
);
