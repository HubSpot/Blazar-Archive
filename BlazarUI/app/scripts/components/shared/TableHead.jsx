import React, {Component, PropTypes} from 'react';
import {map} from 'underscore';
import TableHeadRow from './TableHeadRow.jsx';

class TableHead extends Component {

  getColumns() {
    return map(this.props.columnNames, (column) =>
      <TableHeadRow key={column.key} label={column.label} />
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

TableHead.propTypes = {
  columnNames: PropTypes.array.isRequired
};

export default TableHead;
