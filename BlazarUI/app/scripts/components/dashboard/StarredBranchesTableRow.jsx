import React, {Component, PropTypes} from 'react';
const Link = require('react-router').Link;
import {has} from 'underscore';
import {buildResultIcon, tableRowBuildState, timestampFormatted} from '../Helpers';

import Icon from '../shared/Icon.jsx';
import Sha from '../shared/Sha.jsx';
import CommitMessage from '../shared/CommitMessage.jsx';

class StarredBranchesTableRow extends Component {


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

    let currentState;
    let previousState;

    if (item.get('inProgressBuild') !== undefined) {
      currentState = item.get('inProgressBuild').get('state');
      previousState = latestBuild.get('state');
    }

    else {
      currentState = latestBuild.get('state');
      previousState = '';
    }

    return (
      <tr className={tableRowBuildState(latestBuild.state)}>
        <td className='build-status'>
          {buildResultIcon(currentState, previousState)}
        </td>
        <td>
          <Link to={blazarBranchPath}>{repository}</Link>
        </td>
        <td> 
          {branch}
        </td>
        <td>
          <Link to={latestBuild.get('blazarPath')}>{latestBuild.get('buildNumber')}</Link>
        </td>
        <td>
          {timestampFormatted(latestBuild.get('startTimestamp'))}
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
