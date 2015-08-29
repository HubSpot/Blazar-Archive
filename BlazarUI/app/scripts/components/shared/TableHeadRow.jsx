import React, {Component, PropTypes} from 'react';

class TableHeadRow extends Component {

  render() {

    return (
        <th>{this.props.label}</th>
      );

  }

}

TableHeadRow.propTypes = {
  label: PropTypes.string
};

export default TableHeadRow;
