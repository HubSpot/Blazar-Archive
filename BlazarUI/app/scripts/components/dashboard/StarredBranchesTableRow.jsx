import React, {Component, PropTypes} from 'react';
import {Link} from 'react-router';
import {has} from 'underscore';
import {buildResultIcon, tableRowBuildState, timestampFormatted} from '../Helpers';
import classNames from 'classnames';

import Icon from '../shared/Icon.jsx';
import Sha from '../shared/Sha.jsx';
import CommitMessage from '../shared/CommitMessage.jsx';

let initialState = {
  expanded: false
};

class StarredBranchesTableRow extends Component {

  constructor(props, context) {
    super(props, context);

    this.state = initialState;
  }

  getRowClassNames(state) {
    return classNames([
      'clickable-table-row',
      this.state.expanded ? 'expanded' : ''
    ]);
  }

  onTableClick(blazarPath, e) {
    this.setState({
      expanded: !this.state.expanded
    });
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
      <tr onClick={this.onTableClick.bind(this, buildToUse.get('blazarPath'))} className={this.getRowClassNames(buildToUse.get('state'))}>
        <td className='build-status'>
          {buildResultIcon(buildToUse.get('state'))}
        </td>
        <td className='repo-and-branch'>
          <Link className='repo-link' to={blazarBranchPath}>{repository}</Link>
          <span>{branch}</span>
        </td>
        <td>
          <Link className='build-link' to={buildToUse.get('blazarPath')}>#{buildToUse.get('buildNumber')}</Link>
        </td>
        <td className='timestamp'>
          {timestampFormatted(buildToUse.get('startTimestamp'))}
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
