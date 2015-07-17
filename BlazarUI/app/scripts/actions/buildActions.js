import Reflux from 'reflux';
import $ from 'jQuery';
import parse from './parse';

let BuildActions = Reflux.createActions([
  'loadBuild',
  'loadBuildSuccess',
  'loadBuildError'
]);

BuildActions.loadBuild.preEmit = function(data){

  let endpoint = "/api/builds";

  let promise = $.ajax({
    url: endpoint,
    type: 'GET',
    dataType: 'json'
  });

  promise.success( (resp) => {
    let data = new parse(resp);
    data.addTimeHelpers()
    // grab first build for development purposes
    BuildActions.loadBuildSuccess(data.parsed[0]);
  })

  promise.error( ()=> {
    // To do...
    BuildActions.loadBuildError('an error occured');
  })

};

export default BuildActions;
