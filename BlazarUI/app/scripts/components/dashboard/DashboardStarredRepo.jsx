import React, {Component, PropTypes} from 'react';
import ProgressBar from 'react-bootstrap/lib/ProgressBar';
import { Link } from 'react-router';
import Icon from '../shared/Icon.jsx';

class DashboardStarredRepo extends Component {

  render() {
    console.log(this.props.repo);

    let building = <span></span>;
    let outerClass = 'starredRepo';
    if (this.props.repo.isBuilding) {
      outerClass = 'starredRepo active';
      building = <ProgressBar active now={100} bsStyle="info" className="progressBar-dashboard"/>;
    }

    let config = window.config;
    let repo = this.props.repo;
    let repoLink = `${config.appRoot}/builds/${repo.host}/${repo.organization}/${repo.repository}`;
    let branchLink = `${config.appRoot}/builds/${repo.host}/${repo.organization}/${repo.repository}/${repo.branch}`;

    return (
      <div className={outerClass}>
        <div className="starredRepo-inner">
          <span className="dashboard__repo-name">
            <Link to={repoLink} className="dashboard__title-link">{this.props.repo.repository}</Link>
          </span>
          <Icon type='octicon' name='git-branch' classNames='sidebar__repo-branch-icon' />
          <span className='dashboard__title-link'>
            <Link to={branchLink} className="dashboard__title-link dashboard__branch-link">{repo.branch}</Link>
          </span>

          <div className="dashboard__repo-details">
            details here
          </div>
        </div>
        {building}
      </div>
    );
  }
}

DashboardStarredRepo.propTypes = {
  repo: PropTypes.object
};

export default DashboardStarredRepo;
