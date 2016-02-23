import React, {Component, PropTypes} from 'react';
import BuildStates from '../../constants/BuildStates.js';
import { Link } from 'react-router';
import {LABELS, iconStatus} from '../constants';
import classNames from 'classnames';
import moment from 'moment';

import {tableRowBuildState, timestampFormatted, humanizeText, buildResultIcon, getTableDurationText, buildIsOnDeck, timestampDuration} from '../Helpers';

import Icon from '../shared/Icon.jsx';
import Sha from '../shared/Sha.jsx';


class BranchBuildHistoryTableRow extends Component {

  onTableClick(e) {
    if (e.target.className === 'sha-link') {
      window.open(e.target.href, '_blank');
      return false;
    }
  }

  getRowClassNames(state) {
    return classNames([
      tableRowBuildState(state),
      'branch-build-history-row'
    ]);
  }

  renderSha() {
    const {data, params} = this.props;
    let sha;

    if (data.sha !== undefined) {
      const gitInfo = {
        host: params.host,
        organization: params.org,
        repository: params.repo,
      }

      return (
        <Sha gitInfo={gitInfo} build={data} />  
      );
    }  
  }
  
  renderDuration() {
    const {data} = this.props;
    let duration = data.duration;

    if (data.state === BuildStates.IN_PROGRESS) {
      duration = timestampDuration(data.startTimestamp, moment());
    }

    return getTableDurationText(data.state, duration);
  }
  
  renderStartTime() {
    return timestampFormatted(this.props.data.startTimestamp);
  }

  renderBuildLink() {
    const {data} = this.props;
    
    if (buildIsOnDeck(data.state)) {
      return data.buildNumber;
    }

    return (
      <Link to={data.blazarPath}>{data.buildNumber}</Link>
    );
  }

  render() {
    const {data, params} = this.props;

    setTimeout(() => {
      this.forceUpdate();
    }, 1000);

    return (
      <Link onClick={this.onTableClick.bind(this)} to={data.blazarPath}>
        <tr className={this.getRowClassNames(data.state)}>
          <td className='build-status'>
            {buildResultIcon(data.state)}
          </td>
          <td className='build-result-link'>
            <span>{this.renderBuildLink()}</span>
          </td>
          <td>
            {this.renderStartTime()}
          </td>
          <td>
            {this.renderDuration()}
          </td>
          <td>
            {this.renderSha()}
          </td>
        </tr>
      </Link>
    );
  }
}

BranchBuildHistoryTableRow.propTypes = {
  data: PropTypes.object.isRequired
};

export default BranchBuildHistoryTableRow;
