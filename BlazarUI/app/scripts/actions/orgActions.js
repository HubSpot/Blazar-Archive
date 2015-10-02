import Reflux from 'reflux';

const OrgActions = Reflux.createActions([
  'getRepos',
  'setParams'
]);

OrgActions.loadRepos = function(params) {
  OrgActions.setParams(params);
};

export default OrgActions;
