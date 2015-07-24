import React, {Component, PropTypes} from 'react';
import BuildHistoryTableHeader from './BuildHistoryTableHeader.jsx';
import BuildHistoryTableRow from './BuildHistoryTableRow.jsx';

class BuildHistoryTable extends Component {

  getRows() {
    let builds = this.props.buildHistory;
    return builds.map((build, i) =>
      <BuildHistoryTableRow
        build={builds[i]}
        key={i}
     />
   );

  }

  render() {
    if (this.props.loading) {
      return <div></div>;
    }

    let columnNames = [
      {label: '', key: 'result'},
      {label: 'Build', key: 'buildNumber'},
      {label: 'Start Time', key: 'startTime'},
      {label: 'Duration', key: 'duration'},
      {label: 'Commit', key: 'commit'}
    ];

    // TO DO: empty table
    return (
      <table className="table table-hover table-striped roomy">
        <BuildHistoryTableHeader
          columnNames={columnNames}
        />
        <tbody>
          {this.getRows()}
        </tbody>
      </table>

    );
  }

}


BuildHistoryTable.propTypes = {
  loading: PropTypes.bool,
  buildHistory: PropTypes.array.isRequired
};


export default BuildHistoryTable;
