import React, {Component, PropTypes} from 'react';
import { bindAll } from 'underscore';
import Module from './Module.jsx';
import BuildingIcon from '../shared/BuildingIcon.jsx';
import Icon from '../shared/Icon.jsx';
import Star from '../shared/Star.jsx';
import { Link } from 'react-router';

class BuildsSidebarListItem extends Component {

  constructor() {

    this.state = {
      expanded: false
    };

    bindAll(this, ['handleModuleExpand']);
  }

  handleModuleExpand() {
    this.props.moduleExpandChange(this.props.repo.id);
  }

  getModulesClassNames() {
    let classNames = 'sidebar__modules';
    if (this.props.isExpanded) {
      classNames += ' expanded';
    }
    return classNames;
  }

  getExpandStatus() {
    return this.props.isExpanded ? 'chevron-down' : 'chevron-right';
  }

  componentWillReceiveProps() {
    this.setState({ expanded: this.props.isExpanded });
  }

  render() {
    let config = window.config;
    let repo = this.props.repo;
    let modules = this.props.repo.modules;
    let moduleList = [];
    let repoLink = `${config.appRoot}/builds/${repo.host}/${repo.organization}/${repo.repository}`;
    let branchLink = `${config.appRoot}/builds/${repo.host}/${repo.organization}/${repo.repository}/${repo.branch}`;

    modules.forEach( (build) => {
      moduleList.push(
        <Module key={build.modulePath} repo={build} />
      );
    });

    function getRepoBuildState() {
      if (repo.isBuilding) {
        return <BuildingIcon result='IN_PROGRESS' />;
      }
    }

    return (
      <div className='sidebar__repo-container'>
        <div className='sidebar__repo' onClick={this.handleModuleExpand}>
          <div className='sidebar__build-detail'>
            {getRepoBuildState()}
            <div className='sidebar__repo-name'>
              <Star repo={repo.repository} branch={repo.branch}></Star>
              <Link to={repoLink}>
                {repo.repository}
              </Link>
              <Icon type='octicon' name='git-branch' classNames='sidebar__repo-branch-icon' />
              <span className='sidebar__repo-branch'><Link to={branchLink}>{repo.branch}</Link></span>
            </div>
          </div>
          <Icon name={this.getExpandStatus()} classNames='sidebar__expand' />
        </div>
        <div className={this.getModulesClassNames()}>
          {moduleList}
        </div>
      </div>
    );
  }
}

BuildsSidebarListItem.propTypes = {
  repo: PropTypes.object,
  project: PropTypes.object,
  filterText: PropTypes.string,
  isExpanded: PropTypes.bool,
  moduleExpandChange: PropTypes.func
};

export default BuildsSidebarListItem;
