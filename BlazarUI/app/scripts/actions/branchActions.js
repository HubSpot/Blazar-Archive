import Reflux from 'reflux';
import $ from 'jQuery';

import Builds from '../collections/Builds';

let BranchActions = Reflux.createActions([
  'loadModules',
  'loadModulesSuccess',
  'loadModulesError'
]);

BranchActions.loadModules.preEmit = function(data) {

  let builds = new Builds();
  let promise = builds.fetch();

  promise.done( () => {
    let branch = builds.getBranchModules(data);
    BranchActions.loadModulesSuccess(branch.modules);
  });

  promise.error( (err) => {
    console.warn('Error connecting to the API. Check that you are connected to the VPN');
    BranchActions.loadModulesError('an error occured');
  })


};

export default BranchActions;
