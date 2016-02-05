import React, {Component, PropTypes} from 'react';
import classnames from 'classnames';
import {has, contains} from 'underscore';
import {truncate} from '../Helpers.js';
import BuildingIcon from '../shared/BuildingIcon.jsx';
import Icon from '../shared/Icon.jsx';
import IconStack from '../shared/IconStack.jsx';
import Star from '../shared/Star.jsx';
import Immutable from 'Immutable'
import {getBuildStatusIconClassNames} from '../Helpers.js';
import {BUILD_ICONS} from '../constants.js';

import {Link} from 'react-router'

class SidebarItem extends Component {

  getItemClasses() {
    return classnames([
      'sidebar-item',
      this.props.classNames
    ]);
  }
  
  renderRepoLink() {
    const {gitInfo} = this.props;
    
    return (
      <div className='sidebar-item__repo-link'>
        <Icon type='octicon' name='repo' classNames='icon-muted'/>{ ' ' }
        <Link to={gitInfo.blazarBranchPath} className='sidebar-item__module-branch-name'>
          {truncate(gitInfo.repository, 30, true)}
        </Link>
      </div>
    );
  }

  renderBranchText() {
    const {gitInfo} = this.props;

    return (
      <div className='sidebar-item__branch-link'>
        <Icon type='octicon' name='git-branch' classNames='icon-muted'/>{ ' ' }
        <span className='sidebar-item__module-branch-name'>
          {truncate(gitInfo.branch, 40, true)}
        </span>
      </div>
    )
  }
  
  renderBuildLink() {
    const {build, prevBuildState} = this.props;
    let icon, buildIdLink;

    const nameList = Immutable.List.of(BUILD_ICONS[build.state]);
    
    if (prevBuildState) {
      icon = (
        <Link to={build.blazarPath} className='sidebar-item__building-icon-link'>
          <IconStack classNames={getBuildStatusIconClassNames(build.state, prevBuildState)} iconStackBase="circle" iconNames={nameList} />
        </Link>
      );

      buildIdLink =(
        <Link to={build.blazarPath} className='sidebar-item__build-number'>
          #{build.buildNumber}
        </Link>
      );  
    }

    // never built on blazar
    else {
      icon = (
        <div className='sidebar-item__building-icon-link'>
          <BuildingIcon result='never-built' size='small' />
        </div>
      );
    }
    
    return (
      <div>
        {icon}
        {buildIdLink}
      </div>
    );
  }

  render() {    
    const {build, gitInfo} = this.props;

    return (
      <li className={this.getItemClasses()}>
        {this.renderBuildLink()}
        {this.renderRepoLink()}
        {this.renderBranchText()}
      </li>
    )
  }
}

SidebarItem.propTypes = {
  isStarred: PropTypes.bool,
  build: PropTypes.object.isRequired
};

export default SidebarItem;
