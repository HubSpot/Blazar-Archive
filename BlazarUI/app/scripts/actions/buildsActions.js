/*global config*/
import Reflux from 'reflux';
import Builds from '../collections/Builds';
import StarStore from '../stores/starStore';

const BuildsActions = Reflux.createActions([
  'loadBuilds',
  'loadBuildsSuccess',
  'loadBuildsError',
  'loadBuildOfType',
  'stopListening',
  'setFilterType'
]);

let stars = [];
let filterType;
let filterHasChanged = false;
let shouldPoll = true;
let initialStarLoad;
let subscribedStores = {};
let buildCollections = {
  starred: {},
  all: {},
  building: {}
};

BuildsActions.loadBuilds.preEmit = function(newFilterType) {
  filterType = newFilterType;
  shouldPoll = true;
  initialStarLoad = true;

  // Keep the starred sidebar list up to date
  subscribedStores.unsubscribeFromStars = StarStore.listen(onStarStatusChange);
  
  // Starred toggle needs to load stars before we can poll
  if (filterType !== 'starred') {
    _pollForBuilds();
  }
};

// Change type of build we want to view (sidebar toggle buttons)
BuildsActions.loadBuildOfType = function(newFilterType) {
  filterType = newFilterType;
  filterHasChanged = true;

  _fetchBuilds(() => {
    filterHasChanged = false;
  });
  
};

BuildsActions.stopListening = function() {
  shouldPoll = false;
}

BuildsActions.setFilterType = function(newFilterType) {
  filterType = newFilterType;
}

// When user toggles a star update the sidebar
// Also run on initial page load as starred is default
function onStarStatusChange(newStars) {
  stars = newStars.stars;

  if (filterType !== 'starred') {
    return;
  }
  
  if (stars.length === 0) {
    BuildsActions.loadBuildsSuccess([], 'starred', true);
    return;
  }
  
  if (filterType === 'starred' && initialStarLoad) {
    _pollForBuilds();
    initialStarLoad = false;
  } 
  else {
    _fetchBuilds();
  }
};

function _pollForBuilds() {
  if (!shouldPoll || (filterType === 'starred' && stars.length === 0)) {
    return;
  }

  (function _doPoll() {
    if (!shouldPoll) {
      return;
    }
    _fetchBuilds(() => {
      setTimeout(_doPoll, config.buildsRefresh);
    });
  })();  
}

function _fetchBuilds(cb) {
  buildCollections[filterType] = new Builds({
    request: filterType,
    stars: stars
  });

  const promise = buildCollections[filterType].fetch();

  promise.done( () => {
    BuildsActions.loadBuildsSuccess(buildCollections[filterType].get(), filterType, filterHasChanged);
  });

  promise.always( () => {
    if (typeof cb === 'function') {
      cb();
    }
  });

  promise.error( (err) => {
    console.warn('Error connecting to the API. Check that you are connected to the VPN ', err);
    // BuildsActions.loadBuildsError('an error occured');
  });
}

export default BuildsActions;
