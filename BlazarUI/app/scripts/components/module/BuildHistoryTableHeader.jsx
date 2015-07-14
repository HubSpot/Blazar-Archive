import React from 'react';
import {map} from 'underscore';

class BuildHistoryTableHeader extends React.Component{

  constructor(props, context) {
   super(props);
  }

  getColumns(){
    return map(this.props.columnNames, (column) =>
      <th key={column.key}>{column.label}</th>
    )
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


export default BuildHistoryTableHeader;