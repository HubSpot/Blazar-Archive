import React, {Component, PropTypes} from 'react';
import Icon from '../shared/Icon.jsx';
import {labels, iconStatus} from '../constants';
import Helpers from '../ComponentHelpers';
import { Link } from 'react-router';

class ModulesTableRow extends Component {

  getRowClassNames(build) {
    if (build.state === 'FAILED') {
      return 'bgc-danger';
    }
  }

  getBuildResult(build) {
    let result = build.state;
    let classNames = `icon-roomy ${labels[result]}`;

    return (
      <Icon
        name={iconStatus[result]}
        classNames={classNames}
        title={Helpers.humanizeText(result)}
      />
    );
  }

  render() {
    let {
      lastBuild,
      inProgressBuild,
      pendingBuild,
      module,
      gitInfo,
      modulePath
    } = this.props.module;

    let build = (inProgressBuild ? inProgressBuild : lastBuild ? lastBuild : pendingBuild);
    let buildLink = `${window.config.appRoot}builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${gitInfo.branch}/${module.name}/${build.buildNumber}`;

    let commitLink = `https://${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/commit/${build.sha}/`;
    let startTime = Helpers.timestampFormatted(build.startTimestamp);
    let duration = build.duration;

    if (build.state === 'IN_PROGRESS') {
      duration = 'In Progress...';
    }

    return (
      <tr className={this.getRowClassNames(build)}>
        <td>
          <Icon type='octicon' name='file-directory' classNames="repolist-icon" />
          <a href={modulePath}>{module.name}</a>
        </td>
        <td className='build-result-link'>
          {this.getBuildResult(build)}
          <Link to={buildLink}>{build.buildNumber}</Link>
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
