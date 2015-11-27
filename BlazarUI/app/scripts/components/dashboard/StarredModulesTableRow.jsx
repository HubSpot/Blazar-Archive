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

    if (item.builds.length === 0) {
      return (
        <tr>
          <td />
          <td>
            <Link to={item.module.modulePath}>{item.module.moduleName}</Link>
          </td>
          <td> No build history </td>
          <td />
          <td />
          <td />
        </tr>
      );
    }

    const latestBuild = item.builds[0].build;
    const latestBuildGitInfo = item.builds[0].gitInfo;

    let commitMessage;
    let sha;

    if (has(latestBuild, 'commitInfo')){
      commitMessage = (
        <CommitMessage message={latestBuild.commitInfo.current.message} />
      );
    }
    
    if (latestBuild.sha !== undefined) {
      const gitInfo = {
        host: latestBuildGitInfo.host,
        organization: latestBuildGitInfo.organization,
        repository: latestBuildGitInfo.repository
      }

      sha = <Sha gitInfo={gitInfo} build={latestBuild} />;
    }

    return (
      <tr className={tableRowBuildState(latestBuild.state)}>
        <td className='build-status'>
          {buildResultIcon(latestBuild.state)}
        </td>
        <td>
          <Link to={item.module.modulePath}>{item.module.moduleName}</Link>
        </td>
        <td> 
          <Link to={latestBuild.blazarPath}>{latestBuild.buildNumber}</Link>
        </td>
        <td>
          {timestampFormatted(latestBuild.startTimestamp)}
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
  index: PropTypes.number,
};

export default StarredModulesTableRow;
