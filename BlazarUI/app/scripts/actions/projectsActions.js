import Reflux from 'reflux';
import $ from 'jQuery';
import parse from './parse';

let ProjectsActions = Reflux.createActions([
  'loadProjects',
  'loadProjectsSuccess',
  'loadProjectsError'
]);

ProjectsActions.loadProjects.preEmit = function(data) {

  let endpoint = "/api/builds";

  (function doPoll(){

    let promise = $.ajax({
      url: endpoint,
      type: 'GET',
      dataType: 'json'
    });

    promise.done( (resp) => {
      let data = new parse(resp);
      data.addTimeHelpers().groupJobs();
      ProjectsActions.loadProjectsSuccess(data);
    });

    promise.always( () => {
      setTimeout(doPoll, 5000);
    });

    promise.error( () => {
      ProjectsActions.loadProjectsError('an error occured');
    })


  })();

};

export default ProjectsActions;
