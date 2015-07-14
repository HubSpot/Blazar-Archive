import React from 'react';
import {map} from 'underscore';

class BuildHistoryTableRow extends React.Component{

  constructor(props, context) {
   super(props);
  }

  render() {
    let build = this.props.build;

    return (
      <tr>
        <td>{build.name}</td>
        <td>{build.startTime}</td>
        <td>{build.duration}</td>
      </tr>
    );
  }

}

export default BuildHistoryTableRow;