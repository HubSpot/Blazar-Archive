import React, {Component, PropTypes} from 'react';
import { Link } from 'react-router';
import Icon from '../shared/Icon.jsx';
import {labels, iconStatus} from '../constants';
import Helpers from '../ComponentHelpers';

class ModulesTableRow extends Component {

  getRowClassNames() {
    if (this.props.module.inProgressBuild.result === 'FAILED') {
      return 'bgc-danger';
    }
  }

  getBuildResult() {
    let result = this.props.module.inProgressBuild.result;
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
      inProgressBuild,
      module,
      gitInfo,
      modulePath
    } = this.props.module;
    console.log(this.props.module);

    // to do: generate commit link in build collection
    let commitLink = `https://${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/commit/${inProgressBuild.sha}/`;
    let startTime = Helpers.timestampFormatted(inProgressBuild.startTime);
    let duration = inProgressBuild.duration;

    if (inProgressBuild.result === 'IN_PROGRESS') {
      duration = 'In Progress...';
    }

    return (
      <tr className={this.getRowClassNames()}>
      <td className='build-result-link'>
        {this.getBuildResult()}
        <Link to={inProgressBuild.buildLink}>{inProgressBuild.buildNumber}</Link>
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
          <a href={commitLink} target="_blank">{inProgressBuild.sha}</a>
        </td>
      </tr>
    );
  }
}

ModulesTableRow.propTypes = {
  module: PropTypes.object.isRequired
};

export default ModulesTableRow;
