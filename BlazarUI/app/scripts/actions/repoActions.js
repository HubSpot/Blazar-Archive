import Reflux from 'reflux';

const RepoActions = Reflux.createActions([
  'getBranches',
  'setParams'
]);

RepoActions.loadBranches = function(params) {
  RepoActions.setParams(params);
};

export default RepoActions;
