import React, {Component, PropTypes} from 'react';
import BuildStates from '../../constants/BuildStates.js';
import { Link } from 'react-router';
import {LABELS, iconStatus} from '../constants';
import {has} from 'underscore';

import Helpers from '../ComponentHelpers';
import {tableRowBuildState} from '../Helpers';

import Icon from '../shared/Icon.jsx';
import Sha from '../shared/Sha.jsx';

class ModulesTableRow extends Component {

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
      gitInfo
    } = this.props.data;

    if (!has(this.props.data, 'lastBuild') ) {
      return (
        <tr> 
          <td>
            <Icon type='octicon' name='file-directory' classNames="icon-roomy icon-muted" />
            <Link to={module.blazarPath.module}>{module.name}</Link>
          </td>  
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
      <tr className={tableRowBuildState(build.state)}>
        <td>
          <Icon type='octicon' name='file-directory' classNames="icon-roomy icon-muted" />
          <Link to={module.blazarPath.module}>{module.name}</Link>
        </td>
        <td className='build-result-link'>
          <Link to={buildLink}>
            {this.getBuildResult(build)}
            {build.buildNumber}
          </Link>
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
  data: PropTypes.object.isRequired
};

export default ModulesTableRow;
