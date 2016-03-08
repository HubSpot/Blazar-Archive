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
  expanded: false,
  height: 67
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
    const heightWithChildren = 67 + (28 * (this.props.builds.length > 0 ? this.props.builds.length - 1 : 0));
    let heightDelta;

    if (this.state.expanded) {
      heightDelta = 67 - heightWithChildren;
    }

    else {
      heightDelta = heightWithChildren - 67;
    }

    this.setState({
      expanded: !this.state.expanded
    });

    this.props.onExpand(heightDelta);
  }

  getBuildToUse(build) {
    const {lastBuild, inProgressBuild} = build;

    if (inProgressBuild !== undefined) {
      return inProgressBuild;
    }

    else if (lastBuild !== undefined) {
      return lastBuild;
    }

    return undefined;
  }

  renderExpandText(numberRemaining = 0) {
    const {builds} = this.props;

    if (builds.length < 2) {
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
            {truncate(repository, 22, true)}
          </span>
      </div>
    );
  }

  renderBranchText(build) {
    const {gitInfo} = build;

    return (
      <span className='sidebar-item__module-branch-name'>
        <Link to={gitInfo.blazarBranchPath}>
          {truncate(gitInfo.branch, 25, true)}
        </Link>
      </span>
    );
  }

  renderBuildIcon(build) {
    const buildToUse = this.getBuildToUse(build);

    if (buildToUse.state === BuildStates.SUCCEEDED) {
      return (<div />);
    }

    return (
      <Link to={buildToUse.blazarPath}>
        <div className='sidebar-item__building-icon-link'>
          {buildResultIcon(buildToUse.state)}
        </div>
      </Link>
    );
  }

  renderBuildNumber(build) {
    const buildToUse = this.getBuildToUse(build);

    if (buildToUse === undefined) {
      return (<span />);
    }

    return (
      <Link to={buildToUse.blazarPath} className='sidebar-item__build-number'>
        #{buildToUse.buildNumber}
      </Link>
    );
  }

  renderBranchRow(build, key) {
    const {gitInfo, lastBuild} = build;
    let buildState = build.inProgressBuild !== undefined ? build.inProgressBuild.state : lastBuild.state;

    return (
      <div key={key} className='sidebar-item__branch-link'>
        {this.renderBuildIcon(build)}
        {this.renderBranchText(build)}
        {this.renderBuildNumber(build)}
      </div>
    );
  }

  renderBranchRows() {
    const {builds} = this.props;
    let realBuilds = builds.slice();

    if (realBuilds.length > 1 && !this.state.expanded) {
      const originalSize = realBuilds.length;
      const splicedBuilds = realBuilds.splice(0, 1);
      const numberRemaining = originalSize - 1;

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
  repository: PropTypes.string.isRequired,
  onExpand: PropTypes.func.isRequired
};

export default SidebarItem;
