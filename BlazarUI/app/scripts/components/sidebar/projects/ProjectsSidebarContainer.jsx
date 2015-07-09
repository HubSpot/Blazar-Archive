import React from 'react';
import ProjectsSidebar from './ProjectsSidebar.jsx';
import ProjectsStore from '../../../stores/projectsStore';
import ProjectsActions from '../../../actions/projectsActions';


class ProjectsSidebarContainer extends React.Component {

  constructor(props){
    super(props);

    this.state = {
      projects : [],
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
      <ProjectsSidebar projects={this.state.projects} loading={this.state.loading} />
    );
  }
}

export default ProjectsSidebarContainer;