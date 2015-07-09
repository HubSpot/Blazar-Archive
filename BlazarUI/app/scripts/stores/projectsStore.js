import Reflux from 'reflux';
import ProjectsActions from '../actions/projectsActions';

var ProjectsStore = Reflux.createStore({

  init() {
    this.projects = [];

    this.listenTo(ProjectsActions.loadProjects, this.loadProjects);
    this.listenTo(ProjectsActions.loadProjectsSuccess, this.loadProjectsSuccess);
    this.listenTo(ProjectsActions.loadProjectsError, this.loadProjectsError);
  },

  loadProjects() {
    this.trigger({
      loading: true
    });
  },

  loadProjectsSuccess(projects) {
    this.projects = projects;

    this.trigger({
      projects : this.projects,
      loading: false
    });
  },

  loadProjectsError(error) {
    this.trigger({
      error : error,
      loading: false
    });
  }

});

export default ProjectsStore;