/*global config*/
import React, {Component, PropTypes} from 'react';
import classnames from 'classnames';
import {has} from 'underscore';

import BuildingIcon from '../shared/BuildingIcon.jsx';
import Icon from '../shared/Icon.jsx';
import Star from '../shared/Star.jsx';

let Link = require('react-router').Link;

class SidebarItem extends Component {

  getClassNames() {

    return classnames([
       'sidebar-item',
       this.props.classNames
    ]);
  }

  render() {

    const build = this.props.build;
    let icon;

    if (has(build, 'inProgressBuild')) {
      icon = (
        <Link to={config.appRoot + build.inProgressBuild.blazarPath} className='sidebar-item__building-icon-link'>
          <BuildingIcon result={build.inProgressBuild.state} size='small' />
        </Link>
      );

    } else {
      if (has(build, 'lastBuild')) {
        icon = (
          <Link to={build.lastBuild.blazarPath} className='sidebar-item__building-icon-link'>
            <BuildingIcon result={build.lastBuild.state} size='small' />
          </Link>
        );
      } else {
        icon = (
          <div className='sidebar-item__building-icon-link'>
            <BuildingIcon result='never-built' size='small' />
          </div>
        );
      }
    }

    const star = (
      <Star 
        isStarred={this.props.isStarred}
        toggleStar={this.props.toggleStar} 
        moduleId={this.props.build.module.id} />
    );

    const moduleLink = (
      <Link to={build.module.blazarPath} className='sidebar-item__module-name'>
        {build.module.name}
      </Link>
    );


    const repoLink = (
      <div className='sidebar-item__repo-link'>
        <Icon type='octicon' name='repo' classNames='icon-muted'/>{ ' ' }
        <Link to={build.gitInfo.branchBlazarPath} className='sidebar-item__module-branch-name'>
          {build.gitInfo.repository}
        </Link>
      </div>
    );

    const branchLink = (
      <div className='sidebar-item__branch-link'>
        <Icon type='octicon' name='git-branch' classNames='icon-muted'/>{ ' ' }
        <Link to={build.gitInfo.branchBlazarPath} className='sidebar-item__module-branch-name'>
          {build.gitInfo.branch}
        </Link>
      </div>
    );

    return (
      <li className={this.getClassNames()}>
        {icon}
        {moduleLink}
        {repoLink}
        {branchLink}
        {star}
      </li>
    )
  }
}

SidebarItem.propTypes = {
  isStarred: PropTypes.bool,
  build: PropTypes.object.isRequired,
  toggleStar: PropTypes.func.isRequired
};

export default SidebarItem;
