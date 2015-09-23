import React, {Component, PropTypes} from 'react';
import BuildStates from '../../constants/BuildStates.js';
import { Link } from 'react-router';
import {LABELS, iconStatus} from '../constants';
import {has} from 'underscore';

import Helpers from '../ComponentHelpers';

import Icon from '../shared/Icon.jsx';
import Sha from '../shared/Sha.jsx';

class ModulesTableRow extends Component {

  getRowClassNames(build) {
    if (build.state === BuildStates.FAILED) {
      return 'bgc-danger';
    }
  }

  getBuildResult(build) {
    const result = build.state;
    const classNames = `icon-roomy ${LABELS[result]}`;

    return (
      <Icon
        name={iconStatus[result]}
        classNames={classNames}
        title={Helpers.humanizeText(result)}
      />
    );
  }

  render() {

    const {
      lastBuild,
      inProgressBuild,
      pendingBuild,
      module,
      gitInfo,
      modulePath
    } = this.props.module;

    if (!has(this.props.module, 'lastBuild')) {
      return (
        <tr> 
          <td>{module.name}</td>  
          <td>No History</td>
          <td></td>
          <td></td>
          <td></td>
        </tr> 
      )
    }


    let sha;
    const build = (inProgressBuild ? inProgressBuild : lastBuild ? lastBuild : pendingBuild);
    const buildLink = `${window.config.appRoot}/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${gitInfo.branch}/${module.name}/${build.buildNumber}`;
    const startTime = Helpers.timestampFormatted(build.startTimestamp);

    if (build.sha !== undefined) {
      sha = <Sha gitInfo={gitInfo} build={build} />;
    }

    let duration = build.duration;
    if (build.state === BuildStates.IN_PROGRESS) {
      duration = 'In Progress...';
    }

    return (
      <tr className={this.getRowClassNames(build)}>
        <td>
          <Icon type='octicon' name='file-directory' classNames="icon-roomy icon-muted" />
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
