import Reflux from 'reflux';
import Builds from '../collections/Builds';
import Poller from '../utils/poller';
import BranchSearch from '../collections/BranchSearch';

const BranchActions = Reflux.createActions([
  'loadModulesSuccess',
  'loadModulesError',
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

  branchIds.fetch()
    .done((branchIds) => {
      if (branchIds.length === 0) {
        BranchActions.loadModulesSuccess([]);
      }

      else {
        _createPoller(branchIds);  
      }      
    })
    .fail((jqXHR, textStatus, errorThrown) => {
      BranchActions.loadModulesError(`${jqXHR.status}: ${jqXHR.statusText}`);
    });
};

function _createPoller(branchIds) {

  const collection = new Builds({
    request: 'branchIds',
    branchIds: branchIds,
    mergeOnFetch: true
  });

  poller = new Poller({
    collection: collection
  });

  poller.startPolling((jqXHR) => {
    if (jqXHR.textStatus === 'success') {
      BranchActions.loadModulesSuccess(collection.sortByModuleName());
    }

    else {
      BranchActions.loadModulesError(`${jqXHR.status}: ${jqXHR.statusText}`);
    }
  });

}
    
BranchActions.stopPolling = function() {
  if (poller) {
    poller.stopPolling();
  }
};

export default BranchActions;
