import Reflux from 'reflux';
import $ from 'jQuery';

import Builds from '../collections/Builds';

let BuildsActions = Reflux.createActions([
  'loadBuilds',
  'loadBuildsSuccess',
  'loadBuildsError'
]);

BuildsActions.loadBuilds.preEmit = function(data) {

  (function doPoll(){

    let builds = new Builds();
    let promise = builds.fetch();

    promise.done( () => {
      BuildsActions.loadBuildsSuccess(builds.data);
    });

    promise.always( () => {
      setTimeout(doPoll, 15000);
    });

    promise.error( () => {
      BuildsActions.loadBuildsError('an error occured');
    })


  })();

};

export default BuildsActions;
