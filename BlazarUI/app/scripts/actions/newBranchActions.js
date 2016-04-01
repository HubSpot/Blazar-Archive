/*global config*/
import Reflux from 'reflux';

const NewBranchActions = Reflux.createActions([
  'loadBranchBuildHistory',
  'loadBranchInfo',
  'loadBranchModules',
  'loadBranchMalformedFiles'
]);

export default NewBranchActions;
