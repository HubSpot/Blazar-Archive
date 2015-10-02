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
    fetchBuilds(() => {
      setTimeout(doPoll, config.buildsRefresh);
    });
  })();

};

BuildsActions.fetchBuilds = () => {
  fetchBuilds();
};

function fetchBuilds(cb) {
  const builds = new Builds();
  const promise = builds.fetch();

  promise.done( () => {
    BuildsActions.loadBuildsSuccess(builds.get());
  });

  promise.always( () => {
    if (typeof cb === 'function') {
      cb();
    }
  });

  promise.error( (err) => {
    console.warn('Error connecting to the API. Check that you are connected to the VPN ', err);
    // To do - make note in the view
    BuildsActions.loadBuildsError('an error occured');
  });

}



export default BuildsActions;
