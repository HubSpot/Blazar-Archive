import React, {Component, PropTypes} from 'react';
import {Link} from 'react-router'
import classnames from 'classnames';
import {has, contains} from 'underscore';
import {truncate} from '../Helpers.js';
import BuildingIcon from '../shared/BuildingIcon.jsx';
import Icon from '../shared/Icon.jsx';
import Star from '../shared/Star.jsx';
import BuildStates from '../../constants/BuildStates.js';
import {buildResultIcon} from '../Helpers.js';

let initialState = {
  expanded: false
};

class SidebarItem extends Component {

  constructor() {
    this.state = initialState;
  }

  getItemClasses() {
    return classnames([
      'sidebar-item',
      this.props.classNames
    ]);
  }

  toggleExpand() {
    this.setState({
      expanded: !this.state.expanded
    });
  }

  renderExpandText(numberRemaining = 0) {
    const {builds} = this.props;

    if (builds.length < 4) {
      return '';
    }

    let toggleExpandMessage;

    if (!this.state.expanded) {
      toggleExpandMessage = `show ${numberRemaining} more`;
    }

    else {
      toggleExpandMessage = `show fewer`;
    }

    return (
      <div onClick={this.toggleExpand.bind(this)} className='sidebar-item__and-more'>
        {toggleExpandMessage}
      </div>
    );
  }
  
  renderRepoLink() {
    const {repository} = this.props;
    
    return (
      <div className='sidebar-item__repo-link'>
        <Icon type='octicon' name='repo' classNames='repo-octicon'/>{ '   ' }
          <span className='sidebar-item__module-repo-name'>
            {truncate(repository, 20, true)}
          </span>
      </div>
    );
  }

  renderBranchText(build) {
    const {gitInfo, lastBuild, inProgressBuild} = build;

    let buildToUse;

    if (lastBuild === undefined) {
      if (inProgressBuild === undefined) {
        return (<span />);
      }

      buildToUse = inProgressBuild;
    }

    else {
      buildToUse = lastBuild;
    }

    return (
      <span className='sidebar-item__module-branch-name'>
        <Icon type='octicon' name='git-branch' classNames='repo-octicon'/>{ '   ' }
        <Link to={buildToUse.blazarPath}>
          {truncate(gitInfo.branch, 20, true)}
        </Link>
      </span>
    );
  }

  renderBuildIcon(buildState) {
    if (buildState === BuildStates.SUCCEEDED) {
      return (<div />);
    }

    return (
      <div className='sidebar-item__building-icon-link'>
        {buildResultIcon(buildState)}
      </div>
    );
  }

  renderBuildNumber(build) {
    const {lastBuild} = build;

    if (lastBuild === undefined) {
      return (<span />);
    }

    return (
      <Link to={lastBuild.blazarPath} className='sidebar-item__build-number'>
        #{lastBuild.buildNumber}
      </Link>
    );
  }

  renderBranchRow(build, key) {
    const {gitInfo, lastBuild} = build;
    let buildState = build.inProgressBuild !== undefined ? build.inProgressBuild.state : lastBuild.state;

    return (
      <div key={key} className='sidebar-item__branch-link'>
        {this.renderBuildIcon(buildState)}
        {this.renderBranchText(build)}
        {this.renderBuildNumber(build)}
      </div>
    );
  }

  renderBranchRows() {
    const {builds} = this.props;
    let realBuilds = builds.slice();

    if (realBuilds.length > 3 && !this.state.expanded) {
      const originalSize = realBuilds.length;
      const splicedBuilds = realBuilds.splice(0, 3);
      const numberRemaining = originalSize - 3;

      return (
        <div>
          {splicedBuilds.map((build, i) => {return this.renderBranchRow(build, i);})}       
          {this.renderExpandText(numberRemaining)}
        </div>
      );
    } 

    else {
      return (
        <div>
          {realBuilds.map((build, i) => {return this.renderBranchRow(build, i);})}
          {this.renderExpandText()}
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
