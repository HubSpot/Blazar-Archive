import React, {Component, PropTypes} from 'react';
import classnames from 'classnames';
import {has, contains} from 'underscore';
import {truncate} from '../Helpers.js';
import BuildingIcon from '../shared/BuildingIcon.jsx';
import Icon from '../shared/Icon.jsx';
import Star from '../shared/Star.jsx';
import {buildResultIcon} from '../Helpers.js';

import {Link} from 'react-router'

class SidebarItem extends Component {

  getItemClasses() {
    return classnames([
      'sidebar-item',
      this.props.classNames
    ]);
  }
  
  renderRepoLink() {
    const {repository} = this.props;
    
    return (
      <div className='sidebar-item__repo-link'>
        <Icon type='octicon' name='repo' classNames='icon-muted'/>{ ' ' }
          <Link to="http://google.com" className='sidebar-item__module-branch-name'>
            {truncate(repository, 30, true)}
          </Link>
      </div>
    );
  }

  renderBranchText(build) {
    const gitInfo = build.gitInfo;
    const lastBuild = build.lastBuild;

    return (
      <div className='sidebar-item__branch-link'>
        <div className='sidebar-item__building-icon-link'>
          {buildResultIcon(lastBuild.state)}
        </div>
        { ' ' }
        <span className='sidebar-item__module-branch-name'>
          <Link to={lastBuild.blazarPath}>
            {truncate(gitInfo.branch, 40, true)}
          </Link>
        </span>
      </div>
    )
  }

  renderBranchRow(build) {
    const gitInfo = build.gitInfo;
    const lastBuild = build.lastBuild;

    return (
      <div>
        <div className='sidebar-item__building-icon-link'>
          {buildResultIcon(lastBuild.state, 'never-built')}
        </div>
        {this.renderBranchText(build)}
        <Link to={lastBuild.blazarPath} className='sidebar-item__build-number'>
          #{lastBuild.buildNumber}
        </Link>
      </div>
    );
  }

  renderBranchRows() {
    const {builds} = this.props;

    if (builds.length > 3) {
      const originalSize = builds.length;
      const splicedBuilds = builds.splice(0, 3);
      const numberRemaining = originalSize - 3;

      //console.log(this.props.builds.length);

      return (
        <div>
          {splicedBuilds.map((build) => {return this.renderBranchRow(build);})}
          <span className='sidebar-item__and-more'>
            ...and #{numberRemaining} more
          </span>
        </div>
      );
    } else {
      return (
        <div>
          {builds.map((build) => {return this.renderBranchRow(build);})}
        </div>
      )
    }
  }

  render() {
    return (
      <li className={this.getItemClasses()}>
        {this.renderRepoLink()}
        {this.renderBranchRows()}
      </li>
    )
  }
}

SidebarItem.propTypes = {
  isStarred: PropTypes.bool,
  builds: PropTypes.array.isRequired,
  repository: PropTypes.string.isRequired
};

export default SidebarItem;
