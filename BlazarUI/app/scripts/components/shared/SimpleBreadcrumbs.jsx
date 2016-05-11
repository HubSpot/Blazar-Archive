/*global config*/
import React, { Component, PropTypes } from 'react';
import { Link } from 'react-router';

import Icon from './Icon.jsx';

class SimpleBreadcrumbs extends Component {
	constructor(props) {
    super(props);
  }

	getBuildNumber() {
		const {params, data} = this.props;

		if (params.buildNumber !== 'latest') {
			return params.buildNumber;
		}

		return data.build.buildNumber;
	}

  renderRepoCrumb() {
    const {params, repo, branchInfo} = this.props;

    if (!repo) {
      return null;
    }

    const repoLink = `/builds/repo/${branchInfo.repository}`;

    return (
      <span className='simple-breadcrumbs__repo'>
        <Link to={repoLink}>{branchInfo.repository}</Link>
      </span>
    );
  }

  renderBranchCrumb() {
    const {params, branch, branchInfo} = this.props;

    if (!branch) {
      return null;
    }

    const branchLink = `/builds/branch/${params.branchId}`;

    return (
      <span className='simple-breadcrumbs__branch'>
        <Icon type='fa' name='angle-right' />
        <Link to={branchLink}>{branchInfo.branch}</Link>
      </span>
    );
  }

  renderBuildCrumb() {
    const {params, build, branchInfo} = this.props;

    if (!build) {
      return null;
    }

		const buildNumber = this.getBuildNumber();
    const buildLink = `/builds/branch/${params.branchId}/build/${buildNumber}`;

    return (
      <span className='simple-breadcrumbs__build'>
        <Icon type='fa' name='angle-right' />
        <Link to={buildLink}>#{buildNumber}</Link>
      </span>
    );
  }

  render() {
    return (
      <div className='simple-breadcrumbs'>
        {this.renderRepoCrumb()}
        {this.renderBranchCrumb()}
        {this.renderBuildCrumb()}
      </div>
    )
  }
}

SimpleBreadcrumbs.propTypes = {
  repo: PropTypes.bool,
  branch: PropTypes.bool,
  build: PropTypes.bool,
  params: PropTypes.object,
  branchInfo: PropTypes.object
};

export default SimpleBreadcrumbs;
