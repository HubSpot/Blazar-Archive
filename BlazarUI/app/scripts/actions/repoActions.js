import Reflux from 'reflux';

const RepoActions = Reflux.createActions([
  'loadBranches',
  'stopPolling'
]);

export default RepoActions;
