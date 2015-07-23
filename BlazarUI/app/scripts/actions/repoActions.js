import Reflux from 'reflux';
import $ from 'jQuery';

import Builds from '../collections/Builds';

let BranchActions = Reflux.createActions([
  'loadBranches',
  'loadBranchesSuccess',
  'loadBranchesError'
]);

BranchActions.loadBranches.preEmit = function(data) {

  let builds = new Builds();
  let promise = builds.fetch();

  promise.done( () => {
    let branches = builds.getBranchesByRepo(data);
    BranchActions.loadBranchesSuccess(branches);
  });

  promise.error( (err) => {
    console.warn('Error connecting to the API. Check that you are connected to the VPN');
    BranchActions.loadBranchesError('an error occured');
  })


};

export default BranchActions;
