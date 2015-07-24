import React, {Component, PropTypes} from 'react';
import { Link } from 'react-router';
import Icon from '../shared/Icon.jsx';
import {labels, iconStatus} from '../constants';
import Helpers from '../ComponentHelpers';

class ModulesTableRow extends Component {

  getRowClassNames() {
    if (this.props.module.buildState.result === 'FAILED') {
      return 'bgc-danger';
    }
  }

  getBuildResult() {
    let result = this.props.module.buildState.result;
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
      buildState,
      module,
      gitInfo,
      modulePath
    } = this.props.module;

    // to do: generate commit link in build collection
    let commitLink = `https://${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/commit/${buildState.commitSha}/`;
    let startTime = Helpers.timestampFormatted(buildState.startTime);
    let duration = buildState.duration;

    if (buildState.result === 'IN_PROGRESS') {
      duration = 'In Progress...';
    }

    return (
      <tr className={this.getRowClassNames()}>
      <td className='build-result-link'>
        {this.getBuildResult()}
        <Link to={buildState.buildLink}>{buildState.buildNumber}</Link>
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
          <a href={commitLink} target="_blank">{buildState.commitSha}</a>
        </td>
      </tr>
    );
  }
}

ModulesTableRow.propTypes = {
  module: PropTypes.object.isRequired
};

export default ModulesTableRow;
