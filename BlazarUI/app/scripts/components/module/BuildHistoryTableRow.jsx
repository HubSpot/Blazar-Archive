/*global app*/
import React from 'react';
import Helpers from '../ComponentHelpers';
import { Link } from 'react-router';
import Icon from '../shared/Icon.jsx';
import Copyable from '../shared/Copyable.jsx';
import {labels} from '../constants';

class BuildHistoryTableRow extends React.Component {

  handleHoverCommit() {
    console.log('hover');
  }

  handleCopyCommit() {
    console.log('copy');
  }

  getRowClassNames() {
    if (this.props.build.result === 'FAILED') {
      return 'bgc-danger';
    }
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
    let buildLink = `${app.config.appRoot}/${build.host}/${build.organization}/${build.repository}/${build.branch}/${build.module}/${build.buildNumber}`;

    let startTime = Helpers.timestampFormatted(build.startTime);
    let duration = Helpers.timestampDuration(build.endTime - build.startTime);
    let buildNumber = <Link to={buildLink}>{build.buildNumber}</Link>;
    let sha = Helpers.truncate(build.commit, 8);

    return (
      <tr className={this.getRowClassNames()}>
        <td>
          {this.getBuildResult()}
          {buildNumber}
        </td>
        <td>
          {startTime}
        </td>
        <td>
          {duration}
        </td>
        <td>
          <Copyable text={build.commit} click={this.handleCopyCommit} hover={this.handleHoverCommit}>
            <Icon classNames='fa-roomy fa-link' name='clipboard' />
          </Copyable>
          <a href={commitLink} target="_blank">{sha}</a>
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
