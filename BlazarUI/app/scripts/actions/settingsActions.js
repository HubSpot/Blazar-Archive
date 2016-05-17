/*global config*/
import Reflux from 'reflux';

const SettingsActions = Reflux.createActions([
  'loadNotifications',
  'updateNotification',
  'addNotification',
  'loadSlackChannels',
  'deleteNotification',
  // blazar branch settings
  'interProjectBuildOptIn',
  'triggerInterProjectBuilds',
  'loadSettings'
]);

export default SettingsActions;
