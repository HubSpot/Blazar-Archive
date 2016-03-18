import Reflux from 'reflux';
import SettingsActions from '../actions/settingsActions';
import SettingsApi from '../data/SettingsApi';

const SettingsStore = Reflux.createStore({

  listenables: SettingsActions,

  init() {  
    this.notifications = [];
  },

  onLoadNotifications(params) {
    this.params = params;

    SettingsApi.fetchNotifications(params, (resp) => {
      this.notifications = resp;

      this.trigger({
        notifications: this.notifications,
        loading: false
      });
    });
  },

  onAddNotification(channelName) {
    SettingsApi.addNotification(this.params, channelName, (resp) => {
      this.notifications = resp;

      this.trigger({
        notifications: this.notifications,
        loading: false
      })
    });
  },

  onUpdateNotification(notification) {
    SettingsApi.updateNotification(this.params, notification, (resp) => {
      this.notifications = resp;

      this.trigger({
        notifications: this.notifications,
        loading: false
      });
    });
  },

  onLoadSlackChannels() {
    SettingsApi.fetchSlackChannels((resp) => {
      this.slackChannels = resp;

      this.trigger({
        slackChannels: this.slackChannels
      });
    });
  }

});

export default SettingsStore;
