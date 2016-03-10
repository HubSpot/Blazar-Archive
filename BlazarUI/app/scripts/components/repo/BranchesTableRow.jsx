import React, {Component, PropTypes} from 'react';
import BuildStates from '../../constants/BuildStates.js';
import { Link } from 'react-router';
import {LABELS, iconStatus} from '../constants';
import {has} from 'underscore';
import {tableRowBuildState, humanizeText, timestampFormatted, buildResultIcon, timestampDuration} from '../Helpers';
import moment from 'moment';

import Icon from '../shared/Icon.jsx';
import Sha from '../shared/Sha.jsx';

let initialState = {
  moment: moment()
}

class BranchesTableRow extends Component {

  constructor() {
    this.state = initialState;
  }

  componentDidMount() {
    this.interval = setInterval(this.updateMoment.bind(this), 1000);
  }

  componentWillUnmount() {
    clearInterval(this.interval);
  }

  updateMoment() {
    this.setState({
      moment: moment()
    })
  }

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
      duration = timestampDuration(build.startTimestamp, this.state.moment);
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
          {buildResultIcon(build.state)}
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
    if (has(this.props.data, 'lastBuild') || has(this.props.data, 'inProgressBuild') || has(this.props.data, 'pendingBuild')) {
      return this.renderFullTable();
    }

    return this.renderNoHistoryTable();
  }

}

BranchesTableRow.propTypes = {
  data: PropTypes.object.isRequired
};

export default BranchesTableRow;
