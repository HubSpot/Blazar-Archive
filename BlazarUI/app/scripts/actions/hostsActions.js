import Reflux from 'reflux';

const HostsActions = Reflux.createActions([
  'getHosts',
  'setListenStatus'
]);

HostsActions.loadHosts = function() {
  HostsActions.setListenStatus(true);
};

export default HostsActions;
