import React, {Component, PropTypes} from 'react';
import { Link } from 'react-router';
import Icon from '../shared/Icon.jsx';
import {labels, iconStatus} from '../constants';
import Helpers from '../ComponentHelpers';

class ModulesTableRow extends Component {

  getRowClassNames() {
    let build = (this.props.module.inProgressBuild ? this.props.module.inProgressBuild : this.props.module.lastBuild);
    if (build.result === 'FAILED') {
      return 'bgc-danger';
    }
  }

  getBuildResult() {
    let build = (this.props.module.inProgressBuild ? this.props.module.inProgressBuild : this.props.module.lastBuild);
    let result = build.result;
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
    let startTime = Helpers.timestampFormatted(build.startTime);
    let duration = build.duration;

    if (build.result === 'IN_PROGRESS') {
      duration = 'In Progress...';
    }

    return (
      <tr className={this.getRowClassNames()}>
      <td className='build-result-link'>
        {this.getBuildResult()}
        <Link to={build.buildLink}>{build.buildNumber}</Link>
      </td>
        <td>
          <Link to={modulePath}>{module.name}</Link>
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
