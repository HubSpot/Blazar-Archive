/*global config*/
import React, {Component, PropTypes} from 'react';
import classnames from 'classnames';
import {has} from 'underscore';
import {truncate} from '../Helpers.js';
import BuildingIcon from '../shared/BuildingIcon.jsx';
import Icon from '../shared/Icon.jsx';
import Star from '../shared/Star.jsx';

const Link = require('react-router').Link;

class SidebarItem extends Component {

  getClassNames() {

    return classnames([
       'sidebar-item',
       this.props.classNames
    ]);
  }

  render() {

    const build = this.props.build;
    let icon, buildNumberLink;

    if (has(build, 'inProgressBuild')) {
      icon = (
        <Link to={build.inProgressBuild.blazarPath} className='sidebar-item__building-icon-link'>
          <BuildingIcon result={build.inProgressBuild.state} size='small' />
        </Link>
      );

      buildNumberLink =(
        <Link to={build.inProgressBuild.blazarPath} className='sidebar-item__build-number'>
          #{build.inProgressBuild.buildNumber}
        </Link>
      );

    } else {
      if (has(build, 'lastBuild')) {
        icon = (
          <Link to={build.lastBuild.blazarPath} className='sidebar-item__building-icon-link'>
            <BuildingIcon result={build.lastBuild.state} size='small' />
          </Link>
        );

        buildNumberLink = (
          <Link to={build.lastBuild.blazarPath} className='sidebar-item__build-number'>
            #{build.lastBuild.buildNumber}
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

    let star
    if (this.props.isStarred) {
      star = (
        <Star 
          isStarred={this.props.isStarred}
          modulePath={this.props.build.module.blazarPath.module}
          moduleName={this.props.build.module.name}
          moduleId={this.props.build.module.id} 
          disabled={!this.props.isStarred}
          className='sidebar__star'
        />
      );
    }
    
    let buildNumberSpace = 0;
    if (build.lastBuild) {
      buildNumberSpace = build.lastBuild.buildNumber.toString().length
    }

    const moduleLink = (
      <Link title={build.module.name} to={build.module.blazarPath.module} className='sidebar-item__module-name'>
        {truncate(build.module.name, 35 - buildNumberSpace, true)}
      </Link>
    );

    const repoLink = (
      <div className='sidebar-item__repo-link'>
        <Icon type='octicon' name='repo' classNames='icon-muted'/>{ ' ' }
        <Link to={build.module.blazarPath.repo} className='sidebar-item__module-branch-name'>
          {truncate(build.gitInfo.repository, 30, true)}
        </Link>
      </div>
    );

    const branchLink = (
      <div className='sidebar-item__branch-link'>
        <Icon type='octicon' name='git-branch' classNames='icon-muted'/>{ ' ' }
        <Link to={build.module.blazarPath.branch} className='sidebar-item__module-branch-name'>
          {truncate(build.gitInfo.branch, 40, true)}
        </Link>
      </div>
    );

    return (
      <li className={this.getClassNames()}>
        {icon}
        {moduleLink}
        {buildNumberLink}
        {repoLink}
        {branchLink}
        {star}
      </li>
    )
  }
}

SidebarItem.propTypes = {
  isStarred: PropTypes.bool,
  build: PropTypes.object.isRequired
};

export default SidebarItem;
