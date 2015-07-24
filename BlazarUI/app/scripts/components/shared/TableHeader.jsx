import React, {Component, PropTypes} from 'react';
import {map} from 'underscore';

class TableHeader extends Component {

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

TableHeader.propTypes = {
  columnNames: PropTypes.array.isRequired
};

export default TableHeader;
