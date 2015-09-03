/*global config*/
import Reflux from 'reflux';

import Builds from '../collections/Builds';

let BuildsActions = Reflux.createActions([
  'loadBuilds',
  'loadBuildsSuccess',
  'loadBuildsError'
]);

BuildsActions.loadBuilds.preEmit = function() {

  (function doPoll() {
    let builds = new Builds();
    let promise = builds.fetch();

    promise.done( () => {
      let allBuilds = {};
      allBuilds.all = builds.getAllBuilds();

      // refactor this:
      allBuilds.grouped = [];

      BuildsActions.loadBuildsSuccess(allBuilds);
    });

    promise.always( () => {
      setTimeout(doPoll, config.buildsRefresh);
    });

    promise.error( (err) => {
      console.warn('Error connecting to the API. Check that you are connected to the VPN ', err);
      // To do
      BuildsActions.loadBuildsError('an error occured');
    });


  })();

};

export default BuildsActions;
