import React from 'react';
import { bindAll } from 'underscore';
import Module from './Module.jsx';
import BuildingIcon from '../shared/BuildingIcon.jsx';
import Icon from '../shared/Icon.jsx';

class ProjectSidebarListItem extends React.Component {

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
    return this.props.isExpanded ? 'chevron-up' : 'chevron-down';
  }

  componentWillReceiveProps() {
    this.setState({ expanded: this.props.isExpanded });
  }

  render() {

    let repo = this.props.repo;
    let modules = this.props.repo.modules;
    let repoGitInfo = modules[0].gitInfo;
    let repoLink = `${repoGitInfo.host}/${repoGitInfo.organization}/${repoGitInfo.repository}`;

    let moduleList = [];

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
          <div className='sidebar__repo-url'>
            {repoLink}
          </div>
          <div className='sidebar__build-detail'>
            {getRepoBuildState()}
            <div className='sidebar__repo-name'>
              {repo.repository}
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

ProjectSidebarListItem.propTypes = {
  repo: React.PropTypes.object,
  project: React.PropTypes.object,
  filterText: React.PropTypes.string,
  isExpanded: React.PropTypes.bool,
  moduleExpandChange: React.PropTypes.func
};

export default ProjectSidebarListItem;
