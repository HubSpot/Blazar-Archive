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
      let allBuilds = {};
      allBuilds.modules = builds.getModuleList();
      allBuilds.grouped = builds.groupBuilds();
      BuildsActions.loadBuildsSuccess(allBuilds);
    });

    promise.always( () => {
      setTimeout(doPoll, app.config.jobRefresh);
    });

    promise.error( () => {
      BuildsActions.loadBuildsError('an error occured');
    })


  })();

};

export default BuildsActions;
