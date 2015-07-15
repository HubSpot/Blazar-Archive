import React from 'react';

class BuildHistoryTableRow extends React.Component {

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

BuildHistoryTableRow.propTypes = {
  build: React.PropTypes.object.isRequired
};

export default BuildHistoryTableRow;
