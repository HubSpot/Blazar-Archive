import React, {Component, PropTypes} from 'react';
import {Link} from 'react-router';
import classnames from 'classnames';
import {truncate} from '../Helpers.js';
import Icon from '../shared/Icon.jsx';
import BuildStates from '../../constants/BuildStates.js';
import BuildStateIcon from '../shared/BuildStateIcon.jsx';

class SidebarItem extends Component {

  constructor(props) {
    super(props);

    this.state = {
      expanded: false,
      height: 67
    };

    this.toggleExpand = this.toggleExpand.bind(this);
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
    } else {
      heightDelta = heightWithChildren - 67;
    }

    this.setState({
      expanded: !this.state.expanded
    });

    this.props.onExpand(heightDelta);
  }

  getBuildToUse(build) {
    const {lastBuild, inProgressBuild} = build;

    if (inProgressBuild) {
      return inProgressBuild;
    } else if (lastBuild) {
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
    } else {
      toggleExpandMessage = 'show fewer';
    }

    return (
      <div onClick={this.toggleExpand} className="sidebar-item__and-more">
        {toggleExpandMessage}
      </div>
    );
  }

  renderRepoLink() {
    const {repository} = this.props;
    const blazarRepositoryPath = `/builds/repo/${repository}`;

    return (
      <div className="sidebar-item__repo-link">
        <Icon type="octicon" name="repo" classNames="repo-octicon" />{ '   ' }
          <span className="sidebar-item__module-repo-name">
            <Link to={blazarRepositoryPath}>
              {truncate(repository, 22, true)}
            </Link>
          </span>
      </div>
    );
  }

  renderBranchText(build) {
    const {gitInfo} = build;

    return (
      <span className="sidebar-item__module-branch-name">
        <Link to={gitInfo.blazarBranchPath}>
          {gitInfo.branch}
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
        <div className="sidebar-item__building-icon-link">
          <BuildStateIcon buildState={buildToUse.state} />
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
      <Link to={buildToUse.blazarPath} className="sidebar-item__build-number">
        #{buildToUse.buildNumber}
      </Link>
    );
  }

  renderBranchRow(build, key) {
    const {lastBuild} = build;

    if (!build.inProgressBuild && !lastBuild) {
      return null;
    }

    return (
      <div key={key} className="sidebar-item__branch-link">
        {this.renderBuildIcon(build)}
        {this.renderBranchText(build)}
        {this.renderBuildNumber(build)}
      </div>
    );
  }

  renderBranchRows() {
    const {builds} = this.props;
    const realBuilds = builds.slice();

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

    return (
      <div>
        {realBuilds.map((build, i) => {return this.renderBranchRow(build, i);})}
        {this.renderExpandText()}
      </div>
    );
  }

  render() {
    return (
      <li className={this.getItemClasses()}>
        {this.renderRepoLink()}
        {this.renderBranchRows()}
      </li>
    );
  }
}

SidebarItem.propTypes = {
  builds: PropTypes.array.isRequired,
  repository: PropTypes.string.isRequired,
  onExpand: PropTypes.func.isRequired,
  classNames: PropTypes.string
};

export default SidebarItem;
