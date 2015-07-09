import React from 'react';
import ProjectsSidebarListItem from './ProjectsSidebarListItem.jsx'

class ProjectsSidebar extends React.Component {

  constructor(props){
    super(props);
  }

  render() {
    let projects = this.props.projects.map( (project, i) =>
      <ProjectsSidebarListItem key={i} project={project} />
    );
    let loading = this.props.loading ? <div>Loading Projects...</div> : '';

    return (
      <div className='sidebar'>
        <h2>Projects</h2>
        {loading}
        {projects}
      </div>
    );
  }
}

ProjectsSidebar.propTypes = {
  loading : React.PropTypes.bool,
  projects : React.PropTypes.array
}


export default ProjectsSidebar;