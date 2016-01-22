import React, {Component, PropTypes} from 'react';
import BuildStates from '../../constants/BuildStates.js';
import { Link } from 'react-router';
import {LABELS, iconStatus} from '../constants';
import {has} from 'underscore';
import {tableRowBuildState, humanizeText, timestampFormatted, renderBuildStatusIcon} from '../Helpers';

import Icon from '../shared/Icon.jsx';
import Sha from '../shared/Sha.jsx';

class BranchesTableRow extends Component {

  renderBranchLink(gitInfo) {
    const {gitInfo} = this.props.data;

    return (
      <span>
        <Icon for='branch' classNames="icon-roomy icon-muted" />
        <Link to={gitInfo.blazarBranchPath}>{gitInfo.branch}</Link>
      </span>
    );
  }
  
  renderNoHistoryTable() {
    return (
      <tr> 
        <td></td>
        <td>
          {this.renderBranchLink()}
        </td>
        <td>No History</td>
        <td></td>
        <td></td>
        <td></td>
      </tr> 
    )
  } 
  
  renderFullTable() {
    const {
      lastBuild,
      inProgressBuild,
      pendingBuild,
      gitInfo
    } = this.props.data;
    
    let sha, buildLink;
    const build = inProgressBuild ? inProgressBuild : pendingBuild ? pendingBuild : lastBuild;
    let duration = build.duration;

    if (build.state === BuildStates.IN_PROGRESS) {
      duration = 'In Progress...';
    }

    if (build.sha !== undefined) {
      sha = <Sha gitInfo={gitInfo} build={build} />;
    }

    if (build.blazarPath) {
      buildLink = (
        <Link to={build.blazarPath}>
          {build.buildNumber}
        </Link>
      );
    }

    return (
      <tr className={tableRowBuildState(build.state)}>
        <td className='build-status'>
          {renderBuildStatusIcon(build.state)}
        </td>
        <td>
          {this.renderBranchLink(gitInfo)}
        </td>
        <td className='build-result-link'>
          {buildLink}
        </td>
        <td>
          {timestampFormatted(build.startTimestamp)}
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

  render() {
    if (!has(this.props.data, 'lastBuild') ) {
      return this.renderNoHistoryTable();
    }
    else {
      return this.renderFullTable();
    }
  }

}

BranchesTableRow.propTypes = {
  data: PropTypes.object.isRequired
};

export default BranchesTableRow;
