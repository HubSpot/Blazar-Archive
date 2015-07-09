import React from 'react';
import ProjectSidebar from './ProjectSidebar.jsx';
import Sidebar from '../Sidebar.jsx'

class ProjectSidebarContainer extends React.Component {

  constructor(props){
    super(props);
  }

  render() {
    return (
      <Sidebar headline='Project stuff here'>
        <ProjectSidebar
          projectId={this.props.projectId}
        />
      </Sidebar>
    );
  }
}

export default ProjectSidebarContainer;