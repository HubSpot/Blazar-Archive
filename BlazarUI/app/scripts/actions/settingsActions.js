/*global config*/
import Reflux from 'reflux';

const SettingsActions = Reflux.createActions([
  'loadNotifications',
  'updateNotification',
  'addNotification',
  'loadSlackChannels'
]);

export default SettingsActions;
