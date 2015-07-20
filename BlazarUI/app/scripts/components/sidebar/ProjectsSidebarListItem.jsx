import React from 'react';
import { bindAll } from 'underscore';
import Module from './Module.jsx';
import BuildingIcon from '../shared/BuildingIcon.jsx';

class ProjectSidebarListItem extends React.Component {

  constructor(props) {
    super(props);

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
        <div className='sidebar__repo-url'>
          {repoLink}
        </div>
        <div className='sidebar__repo' onClick={this.handleModuleExpand}>
          {getRepoBuildState()}
          {repo.name}
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
  project: React.PropTypes.object
};

export default ProjectSidebarListItem;
