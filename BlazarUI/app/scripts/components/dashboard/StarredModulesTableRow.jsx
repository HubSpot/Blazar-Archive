import React, {Component, PropTypes} from 'react';
const Link = require('react-router').Link;
import {has} from 'underscore';
import {buildResultIcon, tableRowBuildState, timestampFormatted} from '../Helpers';

import Icon from '../shared/Icon.jsx';
import Sha from '../shared/Sha.jsx';
import CommitMessage from '../shared/CommitMessage.jsx';

class StarredModulesTableRow extends Component {


  render() {

    const item = this.props.item;
    const latestBuild = item.get('lastBuild');
    const latestBuildGitInfo = item.get('gitInfo');

    const blazarRepositoryPath = latestBuildGitInfo.get('blazarRepositoryPath');
    const repository = latestBuildGitInfo.get('repository');
    const blazarBranchPath = latestBuildGitInfo.get('blazarBranchPath');
    const branch = latestBuildGitInfo.get('branch');

    if (latestBuild === undefined) {
      return (
        <tr>
          <td />
          <td>
            <Link to={blazarRepositoryPath}>{repository}</Link>
          </td>
          <td>
            <Link to={blazarBranchPath}>{branch}</Link>
          </td>
          <td> No build history </td>
          <td />
          <td />
          <td />
        </tr>
      );
    }

    const commitInfo = latestBuild.get('commitInfo');
    let commitMessage;
    let sha;

    if (commitInfo !== undefined) {
      commitMessage = (
        <CommitMessage message={commitInfo.get('current').get('message')} />
      );
    }
    
    if (latestBuild.get('sha')) {
      const gitInfo = {
        host: latestBuildGitInfo.get('host'),
        organization: latestBuildGitInfo.get('organization'),
        repository: repository
      }

      latestBuild.sha = latestBuild.get('sha');
      sha = <Sha gitInfo={gitInfo} build={latestBuild} />;
    }

    return (
      <tr className={tableRowBuildState(latestBuild.state)}>
        <td className='build-status'>
          {buildResultIcon(latestBuild.get('state'))}
        </td>
        <td>
          <Link to={blazarRepositoryPath}>{repository}</Link>
        </td>
        <td> 
          <Link to={blazarBranchPath}>{branch}</Link>
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
        <td>
          {commitMessage}
        </td>
      </tr>
    );

  }
  
}

StarredModulesTableRow.propTypes = {
  item: PropTypes.object,
};

export default StarredModulesTableRow;
