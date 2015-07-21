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
    this.setState( { expanded: !this.state.expanded } );
  }

  getModulesClassNames() {
    let classNames = 'sidebar__modules';
    if (this.state.expanded) {
      classNames += ' expanded';
    }
    return classNames;
  }

  getExpandStatus() {
    return this.state.expanded ? 'chevron-up' : 'chevron-down';
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

    modules.forEach( (r) => {
      moduleList.push(
        <Module key={r.buildState.buildNumber} repo={r} />
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
              {repo.name}
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
  isExpanded: React.PropTypes.bool
};

export default ProjectSidebarListItem;
