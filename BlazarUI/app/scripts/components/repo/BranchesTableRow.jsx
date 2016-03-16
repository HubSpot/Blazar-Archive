import React, {Component, PropTypes} from 'react';
import BuildStates from '../../constants/BuildStates.js';
import {Link} from 'react-router';
import {LABELS, iconStatus} from '../constants';
import {has} from 'underscore';
import {tableRowBuildState, humanizeText, timestampFormatted, buildResultIcon, timestampDuration} from '../Helpers';
import moment from 'moment';
import classNames from 'classnames';

import Icon from '../shared/Icon.jsx';
import Sha from '../shared/Sha.jsx';

let initialState = {
  moment: moment()
}

class BranchesTableRow extends Component {

  constructor(props, context) {
    super(props, context);

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

  getRowClassNames(state) {
    return classNames([
      tableRowBuildState(state),
      'clickable-table-row'
    ]);
  }

  onTableClick(blazarPath, e) {
    const link = e.target.className;

    if (link === 'branch-link' || link === 'build-link') {
      return;
    }

    else if (link === 'sha-link') {
      window.open(e.target.href, '_blank');
    }

    else if (blazarPath !== undefined) {
      this.context.router.push(blazarPath);
    }
  }

  renderBranchLink(gitInfo) {
    const {gitInfo} = this.props.data;

    return (
      <span>
        <Link className='branch-link' to={gitInfo.blazarBranchPath}>{gitInfo.branch}</Link>
      </span>
    );
  }

  renderBuildResultIcon(state) {
    if (state === BuildStates.SUCCEEDED) {
      return null;
    }

    return buildResultIcon(state);
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
        <Link className='build-link' to={build.blazarPath}>
          {build.buildNumber}
        </Link>
      );
    }

    return (
      <tr onClick={this.onTableClick.bind(this, build.blazarPath)} className={this.getRowClassNames(build.state)}>
        <td className='build-status'>
          {this.renderBuildResultIcon(build.state)}
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

BranchesTableRow.contextTypes = {
  router: PropTypes.object.isRequired
};

BranchesTableRow.propTypes = {
  data: PropTypes.object.isRequired
};

export default BranchesTableRow;
