import React, {Component, PropTypes} from 'react';
import BuildStates from '../../constants/BuildStates.js';
import BuildHistoryTableHeader from './BuildHistoryTableHeader.jsx';
import BuildHistoryTableRow from './BuildHistoryTableRow.jsx';
import EmptyMessage from '../shared/EmptyMessage.jsx';
import Progress from '../../utils/progress';

class BuildHistoryTable extends Component {

  getRows() {
    const builds = this.props.buildHistory;

    return builds.map((build, i) => {
      if (build.build.state === BuildStates.IN_PROGRESS) {
        const progress = Progress(build.build.startTimestamp, builds)

        return (
          <BuildHistoryTableRow
            build={builds[i]}
            key={i}
            progress={progress}
          />
        );
      }

      return (
        <BuildHistoryTableRow
          build={builds[i]}
          key={i}
        />
      );
    });
  }

  render() {

    if (this.props.loading) {
      return <div></div>;
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

    // TO DO: empty table
    return (
      <table className="table table-hover table-striped">
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
