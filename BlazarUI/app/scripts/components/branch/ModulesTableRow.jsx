import React, {Component, PropTypes} from 'react';
import Icon from '../shared/Icon.jsx';
import {labels, iconStatus} from '../constants';
import Helpers from '../ComponentHelpers';

class ModulesTableRow extends Component {

  getRowClassNames() {
    let build = (this.props.module.inProgressBuild ? this.props.module.inProgressBuild : this.props.module.lastBuild);
    if (build.state === 'FAILED') {
      return 'bgc-danger';
    }
  }

  getBuildResult() {
    let build = (this.props.module.inProgressBuild ? this.props.module.inProgressBuild : this.props.module.lastBuild);
    let result = build.state;
    let classNames = `icon-roomy ${labels[result]}`;

    return (
      <Icon
        name={iconStatus[result]}
        classNames={classNames}
      />
    );
  }

  render() {
    let {
      lastBuild,
      inProgressBuild,
      module,
      gitInfo,
      modulePath
    } = this.props.module;

    let build = (inProgressBuild ? inProgressBuild : lastBuild);

    let commitLink = `https://${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/commit/${build.sha}/`;
    let startTime = Helpers.timestampFormatted(build.startTimestamp);
    let duration = build.duration;

    if (build.state === 'IN_PROGRESS') {
      duration = 'In Progress...';
    }

    return (
      <tr className={this.getRowClassNames()}>
        <td>
          <Icon type='octicon' name='file-directory' classNames="repolist-icon" />
          <a href={modulePath}>{module.name}</a>
        </td>
        <td className='build-result-link'>
          {this.getBuildResult()}
          <a href={build.buildLink}>{build.buildNumber}</a>
        </td>
        <td>
          {startTime}
        </td>
        <td>
          {duration}
        </td>
        <td>
          <a href={commitLink} target="_blank">{build.sha}</a>
        </td>
      </tr>
    );
  }
}

ModulesTableRow.propTypes = {
  module: PropTypes.object.isRequired
};

export default ModulesTableRow;
