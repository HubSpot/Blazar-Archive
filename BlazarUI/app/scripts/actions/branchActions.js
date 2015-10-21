import Reflux from 'reflux';
import Builds from '../collections/Builds';
import Poller from '../utils/poller'
import BranchSearch from '../collections/BranchSearch';

const BranchActions = Reflux.createActions([
  'loadModulesSuccess',
  'stopPolling'
]);

let poller;

BranchActions.loadModules = function(params) {

  const branchIds = new BranchSearch({
    params: [
      { property: 'host', value: params.host },
      { property: 'organization', value: params.org }, 
      { property: 'repository', value: params.repo }, 
      { property: 'branch', value: params.branch } 
    ]
  });

  branchIds.fetch().done((branchIds) => {
    _createPoller(branchIds);
  });
}

function _createPoller(branchIds) {

  const collection = new Builds({
    request: 'branchIds',
    branchIds: branchIds,
    mergeOnFetch: true
  });

  poller = new Poller({
    collection: collection
  });

  poller.startPolling((resp) => {
    if (resp.textStatus === 'success') {
      BranchActions.loadModulesSuccess(collection.sortByModuleName());
    }

    else {
      console.warn('Error loading repositories')
      // To do: global error reporting
    }
  });

}
    
BranchActions.stopPolling = function() {
  poller.stopPolling();
}

export default BranchActions;
