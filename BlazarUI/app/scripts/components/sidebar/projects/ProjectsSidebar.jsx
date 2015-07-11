import React from 'react';
import ProjectsSidebarListItem from './ProjectsSidebarListItem.jsx'
import SidebarFilter from '../SidebarFilter.jsx'

class ProjectsSidebar extends React.Component {

  constructor(props){
    super(props);
  }

  render() {
    // To do: replcae loading text with animation
    let loading = this.props.loading ? <div>Loading Projects...</div> : '';
    let repoList = this.props.projects.repos
    let buildingRepos = this.props.projects.buildingRepos;

    let sidebarRepoList = buildingRepos.map( (repo, i) =>
      <ProjectsSidebarListItem key={i} repo={repo} />
    );

    return (
      <div>
        {loading}
        <SidebarFilter
          loading={this.props.loading}
        />
        <div className='sidebar__list'>
          {sidebarRepoList}
        </div>
      </div>
    );
  }
}

ProjectsSidebar.propTypes = {
  loading : React.PropTypes.bool,
  projects : React.PropTypes.object
}

export default ProjectsSidebar;