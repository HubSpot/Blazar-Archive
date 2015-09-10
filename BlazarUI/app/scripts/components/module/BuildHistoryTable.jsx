import React, {Component, PropTypes} from 'react';
import {map, filter, reduce} from 'underscore';
import BuildHistoryTableHeader from './BuildHistoryTableHeader.jsx';
import BuildHistoryTableRow from './BuildHistoryTableRow.jsx';
import EmptyMessage from '../shared/EmptyMessage.jsx';

class BuildHistoryTable extends Component {

  getRows() {
    const builds = this.props.buildHistory;
    const avgTime = this.averageBuildTime();

    return builds.map((build, i) => {
      if (build.build.state === 'IN_PROGRESS') {
        const elapsedTime = new Date().getTime() - build.build.startTimestamp;
        const progress = Math.round((elapsedTime / avgTime) * 100);

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

  filterOutliers(durations) {
    const values = durations.concat();
    values.sort(function(a, b) {
      return a - b;
    });
    const q1 = values[Math.floor((values.length / 4))];
    const q3 = values[Math.ceil((values.length * (3 / 4)))];
    const iqr = q3 - q1;
    const maxValue = q3 + iqr * 1.5;
    const minValue = q1 - iqr * 1.5;
    const filteredValues = values.filter(function(x) {
        return (x < maxValue) && (x > minValue);
    });
    return filteredValues;
  }

  averageBuildTime() {
    // Get build durations from list of builds
    let durations = map(this.props.buildHistory, (build) => {
      let duration = '';
      if (build.build.startTimestamp !== undefined && build.build.endTimestamp !== undefined) {
        duration = build.build.endTimestamp - build.build.startTimestamp;
      }
      return duration ? duration : undefined;
    });
    // Remove invalid values
    durations = filter(durations, (d) => {
      return  d !== undefined && d > 0;
    });
    // Filter outliers if possible
    if (durations.length > 4) {
      durations = this.filterOutliers(durations);
    }
    // Take the average
    return reduce(durations, (sum, d) => {
      return sum + d;
    }) / durations.length;
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
