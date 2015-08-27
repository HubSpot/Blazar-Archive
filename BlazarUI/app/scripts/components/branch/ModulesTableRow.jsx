import React, {Component, PropTypes} from 'react';
import { Link } from 'react-router';
import {labels, iconStatus} from '../constants';
import Helpers from '../ComponentHelpers';

import Icon from '../shared/Icon.jsx';
import Sha from '../shared/Sha.jsx';

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

    let sha;
    let build = (inProgressBuild ? inProgressBuild : lastBuild ? lastBuild : pendingBuild);
    let buildLink = `${window.config.appRoot}/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${gitInfo.branch}/${module.name}/${build.buildNumber}`;

    if (build.sha !== undefined) {
      sha = <Sha gitInfo={gitInfo} build={build} />;
    }

    let startTime = Helpers.timestampFormatted(build.startTimestamp);
    let duration = build.duration;

    if (build.state === 'IN_PROGRESS') {
      duration = 'In Progress...';
    }

    return (
      <tr className={this.getRowClassNames(build)}>
        <td>
          <Icon type='octicon' name='file-directory' classNames="repolist-icon" />
          <Link to={modulePath}>{module.name}</Link>
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
          {sha}
        </td>
      </tr>
    );
  }
}

ModulesTableRow.propTypes = {
  module: PropTypes.object.isRequired
};

export default ModulesTableRow;
