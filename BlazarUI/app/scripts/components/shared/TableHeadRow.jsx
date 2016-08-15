import React, {PropTypes} from 'react';

const TableHeadRow = ({label}) => <th>{label}</th>;

TableHeadRow.propTypes = {
  label: PropTypes.string
};

export default TableHeadRow;
