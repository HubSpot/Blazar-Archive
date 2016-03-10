import React, {Component, PropTypes} from 'react';
const Link = require('react-router').Link;
import {has} from 'underscore';
import {buildResultIcon, tableRowBuildState, timestampFormatted} from '../Helpers';
import classNames from 'classnames';

import Icon from '../shared/Icon.jsx';
import Sha from '../shared/Sha.jsx';
import CommitMessage from '../shared/CommitMessage.jsx';

class StarredBranchesTableRow extends Component {

  constructor(props, context) {
    super(props, context);
  }

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

    else if (e.target.className === 'build-number') {
      this.context.router.push(blazarPath);
    }

    else {
      this.context.router.push(blazarBranchPath);
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
          <Link to={blazarBranchPath}>{repository}</Link>
        </td>
        <td> 
          {branch}
        </td>
        <td>
          <Link className='build-number' to={buildToUse.get('blazarPath')}>{buildToUse.get('buildNumber')}</Link>
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

StarredBranchesTableRow.contextTypes = {
  router: PropTypes.object.isRequired
};

StarredBranchesTableRow.propTypes = {
  item: PropTypes.object,
};

export default StarredBranchesTableRow;
