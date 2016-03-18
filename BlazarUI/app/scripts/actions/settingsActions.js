/*global config*/
import Reflux from 'reflux';

const SettingsActions = Reflux.createActions([
  'loadNotifications',
  'updateNotification',
  'loadSlackChannels'
]);

export default SettingsActions;
