import React from 'react';
import BuildHistoryTableHeader from './BuildHistoryTableHeader.jsx';
import BuildHistoryTableRow from './BuildHistoryTableRow.jsx';

import MockBuilds from './MockBuilds';

class BuildHistoryTable extends React.Component {

  getRows() {
    // mock data
    let builds = MockBuilds;

    return builds.map((build, i) =>
      <BuildHistoryTableRow
        build={builds[i]}
        key={i}
     />
   );

  }

  render() {
    // mock data
    let columnNames = [
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

export default BuildHistoryTable;
