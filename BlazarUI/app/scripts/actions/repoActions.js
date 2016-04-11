import Reflux from 'reflux';

const RepoActions = Reflux.createActions([
  'loadBranches',
  'loadBranchesAndBuilds',
  'stopPolling'
]);

export default RepoActions;
