import Reflux from 'reflux';

const HostsActions = Reflux.createActions([
  'getHosts',
  'setListenStatus'
]);

HostsActions.loadHosts = function(state) {
  HostsActions.setListenStatus(state);

  if (state) {
    HostsActions.getHosts();
  }
};

export default HostsActions;
