import React, { Component, PropTypes } from 'react';
import { Link } from 'react-router';

import Icon from './Icon.jsx';
import { getRepoPath, getBranchStatePath, getBranchBuildPath } from '../../utils/blazarPaths';

class SimpleBreadcrumbs extends Component {

  getBuildNumber() {
    const {params, data} = this.props;
    const getLatestBuildNumber = params.buildNumber === 'latest';
    return getLatestBuildNumber ? data.build.buildNumber : params.buildNumber;
  }

  renderRepoCrumb() {
    const {repo, branchInfo} = this.props;

    if (!repo) {
      return null;
    }

    const repoLink = getRepoPath(branchInfo.repository);

    return (
      <span className="simple-breadcrumbs__repo">
        <Link to={repoLink}>{branchInfo.repository}</Link>
      </span>
    );
  }

  renderBranchCrumb() {
    const {params, branch, branchInfo} = this.props;

    if (!branch) {
      return null;
    }

    const branchLink = getBranchStatePath(params.branchId);

    return (
      <span className="simple-breadcrumbs__branch">
        <Icon type="fa" name="angle-right" />
        <Link to={branchLink}>{branchInfo.branch}</Link>
      </span>
    );
  }

  renderBuildCrumb() {
    const {params, build} = this.props;

    if (!build) {
      return null;
    }

    const buildNumber = this.getBuildNumber();
    const buildLink = getBranchBuildPath(params.branchId, buildNumber);

    return (
      <span className="simple-breadcrumbs__build">
        <Icon type="fa" name="angle-right" />
        <Link to={buildLink}>#{buildNumber}</Link>
      </span>
    );
  }

  render() {
    return (
      <div className="simple-breadcrumbs">
        {this.renderRepoCrumb()}
        {this.renderBranchCrumb()}
        {this.renderBuildCrumb()}
      </div>
    );
  }
}

SimpleBreadcrumbs.propTypes = {
  repo: PropTypes.bool,
  branch: PropTypes.bool,
  build: PropTypes.bool,
  params: PropTypes.object,
  branchInfo: PropTypes.object,
  data: PropTypes.object
};

export default SimpleBreadcrumbs;
