import React, {Component, PropTypes} from 'react';
import {Link, browserHistory} from 'react-router';
import {has} from 'underscore';
import {buildResultIcon, tableRowBuildState, timestampFormatted} from '../Helpers';
import classNames from 'classnames';

import Icon from '../shared/Icon.jsx';
import Sha from '../shared/Sha.jsx';
import CommitMessage from '../shared/CommitMessage.jsx';

class StarredBranchesTableRow extends Component {

  getRowClassNames(state) {
    return classNames([
      tableRowBuildState(state),
      'clickable-table-row'
    ]);
  }

  onTableClick(blazarBranchPath, blazarPath, e) {
    if (e.target.className === 'sha-link') {
      window.open(e.target.href, '_blank');
    }

    else if (e.target.className === 'repo-link') {
      browserHistory.push(blazarBranchPath);
    }

    else {
      browserHistory.push(blazarPath);
    }
  }

  render() {

    const item = this.props.item;
    const latestBuild = item.get('lastBuild');
    const latestBuildGitInfo = item.get('gitInfo');

    const blazarBranchPath = latestBuildGitInfo.get('blazarBranchPath');
    const repository = latestBuildGitInfo.get('repository');
    const branch = latestBuildGitInfo.get('branch');

    if (latestBuild === undefined) {
      return (
        <tr>
          <td />
          <td>
            <Link to={blazarBranchPath}>{repository}</Link>
          </td>
          <td>
            {branch}
          </td>
          <td> No build history </td>
          <td />
          <td />
        </tr>
      );
    }

    const commitInfo = latestBuild.get('commitInfo');
    let sha;
    
    if (latestBuild.get('sha')) {
      const gitInfo = {
        host: latestBuildGitInfo.get('host'),
        organization: latestBuildGitInfo.get('organization'),
        repository: repository
      }

      latestBuild.sha = latestBuild.get('sha');
      sha = <Sha gitInfo={gitInfo} build={latestBuild} />;
    }

    let buildToUse = item.get('inProgressBuild') !== undefined ? item.get('inProgressBuild') : latestBuild;

    return (
      <tr onClick={this.onTableClick.bind(this, blazarBranchPath, buildToUse.get('blazarPath'))} className={this.getRowClassNames(buildToUse.get('state'))}>
        <td className='build-status'>
          {buildResultIcon(buildToUse.get('state'))}
        </td>
        <td>
          <Link className='repo-link' to={blazarBranchPath}>{repository}</Link>
        </td>
        <td> 
          {branch}
        </td>
        <td>
          <Link to={buildToUse.get('blazarPath')}>{buildToUse.get('buildNumber')}</Link>
        </td>
        <td>
          {timestampFormatted(buildToUse.get('startTimestamp'))}
        </td>
        <td>
          {sha}
        </td>
      </tr>
    );

  }
  
}

StarredBranchesTableRow.propTypes = {
  item: PropTypes.object,
};

export default StarredBranchesTableRow;
