import React from 'react';
import ProjectsSidebar from './ProjectsSidebar.jsx';
import ProjectsStore from '../../stores/projectsStore';
import ProjectsActions from '../../actions/projectsActions';
import Sidebar from './Sidebar.jsx';

class ProjectsSidebarContainer extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      projects: {
        grouped: [],
        modules: []
      },
      loading: false
    };
  }

  componentDidMount() {
    this.unsubscribe = ProjectsStore.listen(this.onStatusChange.bind(this));
    ProjectsActions.loadProjects();
  }

  componentWillUnmount() {
    this.unsubscribe();
  }

  onStatusChange(state) {
    this.setState(state);
  }

  render() {
    return (
      <Sidebar>
        <ProjectsSidebar projects={this.state.projects} loading={this.state.loading} />
      </Sidebar>
    );
  }
}

export default ProjectsSidebarContainer;
