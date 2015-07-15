import React from 'react';
import BuildHistoryTableHeader from './BuildHistoryTableHeader.jsx';
import BuildHistoryTableRow from './BuildHistoryTableRow.jsx';

class BuildHistoryTable extends React.Component {

  getRows() {
    // mock data
    let builds = [
      {name: 'Baragon', startTime: '2 days ago', duration: '4 minutes'},
      {name: 'Contacts', startTime: '1 hr ago', duration: '2 minutes'},
      {name: 'Email', startTime: '1 day 2 hrs ago', duration: '6 minutes'}
    ];

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
      {label: 'Name', key: 'name'},
      {label: 'Start Time', key: 'startTime'},
      {label: 'Duration', key: 'duration'}
    ];

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
