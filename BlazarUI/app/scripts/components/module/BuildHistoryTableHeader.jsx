import React, {Component, PropTypes} from 'react';
import {map} from 'underscore';

class BuildHistoryTableHeader extends Component {

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
  columnNames: PropTypes.array.isRequired
};

export default BuildHistoryTableHeader;
