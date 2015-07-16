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
    let modules = [];
    let repoDetail = this.props.repo;
    let moduleGitInfo = repoDetail[0].gitInfo;
    let repoLink = `${moduleGitInfo.host}/${moduleGitInfo.organization}/${moduleGitInfo.repository}`;

    repoDetail.forEach( (repo) => {
      modules.push(
        <Module key={repo.buildState.buildNumber} repo={repo} />
      );
    });


    function getRepoBuildState() {
      if (repoDetail.moduleIsBuilding) {
        return <BuildingIcon status='success' />;
      }
    }

    return (
      <div className='sidebar__repo-container'>
        <div className='sidebar__repo-url'>
          {repoLink}
        </div>
        <div className='sidebar__repo' onClick={this.handleModuleExpand}>
          {getRepoBuildState()}
          {repoDetail.repository}
        </div>
        <div className={this.getModulesClassNames()}>
          {modules}
        </div>
      </div>
    );
  }
}

ProjectSidebarListItem.propTypes = {
  repo: React.PropTypes.array,
  project: React.PropTypes.object
};

export default ProjectSidebarListItem;
