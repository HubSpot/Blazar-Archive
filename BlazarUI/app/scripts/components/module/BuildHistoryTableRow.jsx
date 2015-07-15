import React from 'react';
import Helpers from '../ComponentHelpers';
import Label from '../shared/Label.jsx';
import ReactZeroClipboard from 'react-zeroclipboard';

const labels = {
  'SUCCEEDED': 'success',
  'FAILED': 'danger'
};

class BuildHistoryTableRow extends React.Component {

  handleHoverCommit() {

  }

  handleCopyCommit() {

  }

  render() {
    let build = this.props.build;
    let commitLink = `https://${build.host}/${build.organization}/${build.repository}/commit/${build.commit}/`;
    
    return (
      <tr>
        <td><Label type={labels[build.result]}>{build.buildNumber}</Label></td>
        <td>{Helpers.timestampFormatted(build.startTime)}</td>
        <td>{Helpers.timestampDuration(build.endTime - build.startTime)}</td>
        <td>
          <ReactZeroClipboard text={commitLink}>
            <i onClick={this.handleCopyCommit} onMouseOver={this.handleHoverCommit} className="fa fa-clipboard fa-roomy clickable"></i>
          </ReactZeroClipboard>
          <a href={commitLink} target="_blank">{Helpers.truncate(build.commit, 8)}</a>
        </td>
      </tr>
    );
  }
}

BuildHistoryTableRow.propTypes = {
  build: React.PropTypes.shape({
    buildNumber: React.PropTypes.number,
    startTime: React.PropTypes.number,
    endTime: React.PropTypes.number,
    commit: React.PropTypes.string,
    result: React.PropTypes.oneOf(['SUCCEEDED', 'FAILED'])
  })
};

export default BuildHistoryTableRow;
