import React, {Component, PropTypes} from 'react';
import BuildStates from '../../constants/BuildStates.js';
import { Link } from 'react-router';
import {LABELS, iconStatus} from '../constants';
import {has} from 'underscore';

import Helpers from '../ComponentHelpers';
import {tableRowBuildState} from '../Helpers';

import Icon from '../shared/Icon.jsx';
import Sha from '../shared/Sha.jsx';

class BranchBuildHistoryTableRow extends Component {

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
    const {data} = this.props;
    
    let sha;
    // const buildLink = `${window.config.appRoot}/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${gitInfo.branch}/${module.name}/${build.buildNumber}`;
    const startTime = Helpers.timestampFormatted(data.startTimestamp);

    if (data.sha !== undefined) {
      // sha = <Sha gitInfo={gitInfo} build={build} />;
      sha = data.sha;
    }

    let duration = data.duration;
    if (data.state === BuildStates.IN_PROGRESS) {
      duration = 'In Progress...';
    }

    return (
      <tr className={tableRowBuildState(data.state)}>
        <td>
          
        </td>
        <td className='build-result-link'>
          <Link to={data.blazarPath}>{data.buildNumber}</Link>
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

BranchBuildHistoryTableRow.propTypes = {
  data: PropTypes.object.isRequired
};

export default BranchBuildHistoryTableRow;
