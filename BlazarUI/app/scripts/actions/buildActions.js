import Reflux from 'reflux';
import $ from 'jQuery';

let BuildActions = Reflux.createActions([
  'loadBuild',
  'loadBuildSuccess',
  'loadBuildError'
]);

BuildActions.loadBuild.preEmit = function(data){
  let endpoint = "/js/app/mock.json";
  let promise = $.ajax({ url: endpoint, type: 'GET', dataType: 'json' });

  promise.success( (resp) => {
    BuildActions.loadBuildSuccess(resp[0]);
  })

  promise.error( ()=> {
    // To do...
    BuildActions.loadBuildError('an error occured');
  })

};

export default BuildActions;
