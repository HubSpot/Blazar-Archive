import React from 'react';
import Helpers from '../ComponentHelpers';
import config from '../../config';
import { Link } from 'react-router';
import Icon from '../shared/Icon.jsx';
import Copyable from '../shared/Copyable.jsx';

const labels = {
  'SUCCEEDED': 'success',
  'FAILED': 'danger'
};

class BuildHistoryTableRow extends React.Component {

  handleHoverCommit() {
    console.log('hover');
  }

  handleCopyCommit() {
    console.log('copy');
  }

  getBuildResult() {
    let result = this.props.build.result;
    let type;
    if (result === 'SUCCEEDED') {
      type = 'check';
    } else if (result === 'FAILED') {
      type = 'close';
    } else {
      return '';
    }

    let classNames = 'fa-roomy ' + labels[this.props.build.result];
    return <Icon name={type} classNames={classNames}/>;
  }

  render() {
    let build = this.props.build;
    let commitLink = `https://${build.host}/${build.organization}/${build.repository}/commit/${build.commit}/`;
    let buildLink = `${config.appRoot}/${build.host}/${build.organization}/${build.repository}/${build.branch}/${build.module}/${build.buildNumber}`;
    return (
      <tr>
        <td>
          {this.getBuildResult()}
          <Link to={buildLink}>#{build.buildNumber}</Link>

        </td>
        <td>{Helpers.timestampFormatted(build.startTime)}</td>
        <td>{Helpers.timestampDuration(build.endTime - build.startTime)}</td>
        <td>
          <Copyable text={commitLink} click={this.handleCopyCommit} hover={this.handleHoverCommit}>
            <Icon classNames='fa-roomy' name='clipboard' />
          </Copyable>
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


// <i onClick={this.handleCopyCommit} onMouseOver={this.handleHoverCommit} className="fa fa-clipboard fa-roomy clickable"></i>
