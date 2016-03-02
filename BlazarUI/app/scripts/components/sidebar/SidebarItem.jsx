import React, {Component, PropTypes} from 'react';
import {Link} from 'react-router'
import classnames from 'classnames';
import {has, contains} from 'underscore';
import {truncate} from '../Helpers.js';
import BuildingIcon from '../shared/BuildingIcon.jsx';
import Icon from '../shared/Icon.jsx';
import Star from '../shared/Star.jsx';
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

  renderExpandText(numberRemaining) {
    const {builds} = this.props;

    if (builds.length < 4) {
      return '';
    }

    else if (!this.state.expanded) {
      return (
        <span onClick={this.toggleExpand.bind(this)} className='sidebar-item__and-more'>
          ...show {numberRemaining} more
        </span>
      );
    }

    return (
      <span onClick={this.toggleExpand.bind(this)} className='sidebar-item__and-more'>
        ...hide
      </span>
    );
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

    if (lastBuild === undefined) {
      return (<span />);
    }

    return (
      <span className='sidebar-item__module-branch-name'>
        <Link to={lastBuild.blazarPath}>
          {truncate(gitInfo.branch, 40, true)}
        </Link>
      </span>
    );
  }

  renderBranchRow(build) {
    const gitInfo = build.gitInfo;
    const lastBuild = build.lastBuild;

    if (lastBuild === undefined) {
      return (
        <div>
          {this.renderBranchText(build)}
        </div>
      );
    }

    let buildState = build.inProgressBuild !== undefined ? build.inProgressBuild.state : lastBuild.state;

    return (
      <div className='sidebar-item__branch-link'>
        <div className='sidebar-item__building-icon-link'>
          {buildResultIcon(buildState)}
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
    let realBuilds = builds.slice();

    if (realBuilds.length > 3 && !this.state.expanded) {
      const originalSize = realBuilds.length;
      const splicedBuilds = realBuilds.splice(0, 3);
      const numberRemaining = originalSize - 3;

      return (
        <div>
          {splicedBuilds.map((build) => {return this.renderBranchRow(build);})}       
          {this.renderExpandText(numberRemaining)}
        </div>
      );
    } 

    else {
      return (
        <div>
          {realBuilds.map((build) => {return this.renderBranchRow(build);})}
          {this.renderExpandText(0)}
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
