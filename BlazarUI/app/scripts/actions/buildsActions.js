/*global config*/
import Reflux from 'reflux';

import Builds from '../collections/Builds';

const BuildsActions = Reflux.createActions([
  'loadBuilds',
  'loadBuildsSuccess',
  'loadBuildsError'
]);

BuildsActions.loadBuilds.preEmit = function() {

  (function doPoll() {
    const builds = new Builds();
    const promise = builds.fetch();

    promise.done( () => {
      let allBuilds = {};

      allBuilds.all = builds.get();

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
