import Reflux from 'reflux';

var ProjectsActions = Reflux.createActions([
  'loadProjects',
  'loadProjectsSuccess',
  'loadProjectsError'
]);

ProjectsActions.loadProjects.preEmit = function(data){
  // we will put api call/ async stuff here
  // temporarily using setTimeout for faking async behaviour
  setTimeout(function(){
    var Projects = ['Australia', 'NewZealand', 'Singapore', 'Tonga'];
    ProjectsActions.loadProjectsSuccess(Projects);

    // on error
    // ProjectsActions.loadProjectsError('an error occured');
  },500);
};

export default ProjectsActions;