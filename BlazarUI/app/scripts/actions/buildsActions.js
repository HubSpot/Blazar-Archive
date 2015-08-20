import Reflux from 'reflux';
import $ from 'jquery';

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
      allBuilds.grouped = builds.groupBuildsByRepo();
      BuildsActions.loadBuildsSuccess(allBuilds);
    });

    promise.always( () => {
      setTimeout(doPoll, config.buildsRefresh);
    });

    promise.error( (err) => {
      console.warn('Error connecting to the API. Check that you are connected to the VPN');
      // To do
      BuildsActions.loadBuildsError('an error occured');
    })


  })();

};

export default BuildsActions;
