/*global config*/
import Reflux from 'reflux';
import Builds from '../collections/Builds';

import StarActions from './starActions';
import StarStore from '../stores/starStore';


const StarredBuildsActions = Reflux.createActions([
  'loadBuilds',
  'loadBuildsSuccess',
  'loadBuildsError',
  'loadBuildOfType'
]);


let stars = [];
let filterType;
let filterHasChanged = false;
let shouldPoll = true;
let subscribedStores = {};
let buildCollections = {
  starred: {},
  all: {},
  building: {}
};


// TO DO:
// Refactor this so we are not
// instantiating a new build
// every time we fetch.
// Use a refresh method instead
// and keep upating the builds in the Collection
// instead of as a variable here with the actions.

// initial page load should fetch stars
StarredBuildsActions.loadBuilds.preEmit = function(newFilterType) {  
  filterType = newFilterType;
  subscribedStores.unsubscribeFromStars = StarStore.listen(onStarStatusChange);
};

// change type of build we want to view
StarredBuildsActions.loadBuildOfType = function(newFilterType) {
  filterType = newFilterType;
  filterHasChanged = true;
  _fetchBuilds(() => {
    filterHasChanged = false;
  });
  
};

function onStarStatusChange(newStars) {
  stars = newStars.stars;
  if (filterType === 'starred') {
    _pollForBuilds();
  }
};

function _pollForBuilds() {
  if (!shouldPoll || (filterType === 'starred' && stars.length === 0)) {
    return;
  }

  (function _doPoll() {
    _fetchBuilds(() => {
      setTimeout(_doPoll, config.buildsRefresh);
    });
  })();  
}

StarredBuildsActions.stopListening = function() {
  subscribedStores.unsubscribeFromStars();
  
}

function _fetchBuilds(cb) {

  buildCollections[filterType] = new Builds({
    request: filterType,
    stars: stars
  });

  const promise = buildCollections[filterType].fetch();

  promise.done( () => {
    StarredBuildsActions.loadBuildsSuccess(buildCollections[filterType].get(), filterType, filterHasChanged);
  });

  promise.always( () => {
    if (typeof cb === 'function') {
      cb();
    }
  });

  promise.error( (err) => {
    console.warn('Error connecting to the API. Check that you are connected to the VPN ', err);
    // StarredBuildsActions.loadBuildsError('an error occured');
  });

}



export default StarredBuildsActions;
