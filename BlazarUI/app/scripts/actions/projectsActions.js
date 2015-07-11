import Reflux from 'reflux';
import $ from 'jQuery';
import projectsData from './projectsData';

let ProjectsActions = Reflux.createActions([
  'loadProjects',
  'loadProjectsSuccess',
  'loadProjectsError'
]);

ProjectsActions.loadProjects.preEmit = function(data){
  let endpoint = "http://localhost:1337/js/app/mock.json";
  let promise = $.ajax({ url: endpoint, type: 'GET', dataType: 'json' });

  promise.success( (resp) => {
    projectsData.manageResponse(resp, function(data){
      ProjectsActions.loadProjectsSuccess(data);
    })
  })

  promise.error( ()=> {
    ProjectsActions.loadProjectsError('an error occured');
  })

};

export default ProjectsActions;