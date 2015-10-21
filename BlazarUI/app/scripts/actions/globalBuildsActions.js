import Reflux from 'reflux';
import Builds from '../collections/Builds';
import Poller from '../utils/poller'
import StarStore from '../stores/starStore';

const GlobalBuildsActions = Reflux.createActions([
  'loadBuildsSuccess',
  'stopPolling',
  'loadBuildsOnceSuccess',
  'pollBuilds'
]);

let poller;

const globalBuildsCollection = new Builds({
  request: 'all',
  mergeOnFetch: true
});

GlobalBuildsActions.loadBuilds = function() {

  poller = new Poller({
    collection: globalBuildsCollection
  });

  poller.startPolling((resp) => {
    if (resp.textStatus === 'success') {
      GlobalBuildsActions.loadBuildsSuccess(globalBuildsCollection.data);
    }
    else {
      console.warn('Error loading global builds')
      // GlobalBuildsActions.loadError();
    }
  });

};
  
GlobalBuildsActions.stopPolling = function() {
  if (poller) {
    poller.stopPolling();    
  }

}

GlobalBuildsActions.loadBuildsOnce = function() {
  const promise = globalBuildsCollection.fetch();
  promise.done((data) => {
    GlobalBuildsActions.loadBuildsOnceSuccess(globalBuildsCollection.data);
  })
}

export default GlobalBuildsActions;
