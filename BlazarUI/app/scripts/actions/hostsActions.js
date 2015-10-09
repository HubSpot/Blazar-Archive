import Reflux from 'reflux';

const HostsActions = Reflux.createActions([
  'getHosts',
  'setListenStatus'
]);

HostsActions.loadHosts = function() {
  HostsActions.setListenStatus(true);
  HostsActions.getHosts();
};

export default HostsActions;
