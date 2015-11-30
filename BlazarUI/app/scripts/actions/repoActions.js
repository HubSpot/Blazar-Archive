import Reflux from 'reflux';
import Builds from '../collections/Builds';
import Poller from '../utils/poller';
import BranchSearch from '../collections/BranchSearch';

const RepoActions = Reflux.createActions([
  'loadBranchesSuccess',
  'loadBranchesError',
  'stopPolling'
]);

let poller;

RepoActions.loadBranches = function(params) {

  const branchIds = new BranchSearch({
    params: [
      { property: 'host', value: params.host },
      { property: 'organization', value: params.org }, 
      { property: 'repository', value: params.repo } 
    ]
  });

  branchIds.fetch()
    .done((branchIds) => {
      if (branchIds.length === 0) {
        RepoActions.loadBranchesSuccess([]);
      }
      
      else {
        _createPoller(branchIds);  
      }
    })
    .error((err) => {
      console.warn(err);
      RepoActions.loadBranchesError(`${err.status}: ${err.statusText}`);
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

  poller.startPolling((resp) => {
    if (resp.textStatus === 'success') {
      RepoActions.loadBranchesSuccess(collection.sortByBranchName());
    }

    else {
      RepoActions.loadBranchesError('Error loading repositories');
    }
    
  });
  
  
}
    
RepoActions.stopPolling = function() {
  poller.stopPolling();
};

export default RepoActions;
