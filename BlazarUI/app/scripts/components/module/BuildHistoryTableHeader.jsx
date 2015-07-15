import React from 'react';
import {map} from 'underscore';

class BuildHistoryTableHeader extends React.Component {

  getColumns() {
    return map(this.props.columnNames, (column) =>
      <th key={column.key}>{column.label}</th>
    );
  }

  render() {

    return (
      <thead>
        <tr>
          {this.getColumns()}
        </tr>
      </thead>
    );
  }

}

BuildHistoryTableHeader.propTypes = {
  columnNames: React.PropTypes.array.isRequired
};

export default BuildHistoryTableHeader;
