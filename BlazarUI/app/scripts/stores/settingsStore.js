import Reflux from 'reflux';
import SettingsActions from '../actions/settingsActions';
import SettingsApi from '../data/SettingsApi';

const SettingsStore = Reflux.createStore({

  listenables: SettingsActions,

  init() {
    this.notifications = [];
    this.triggerInterProjectBuilds = false;
    this.interProjectBuildOptIn = false;
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

  onLoadSettings(params) {
    this.params = params;

    SettingsApi.getSettings(params, (resp) => {
      this.triggerInterProjectBuilds = resp.triggerInterProjectBuilds;
      this.interProjectBuildOptIn = resp.interProjectBuildOptIn;
      this.trigger({
        triggerInterProjectBuilds: this.triggerInterProjectBuilds,
        interProjectBuildOptIn: this.interProjectBuildOptIn,
        loadingSettings: false
      });
    });
  },

  onAddNotification(channelName) {
    SettingsApi.addNotification(this.params, channelName, (resp) => {
      this.notifications = resp;

      this.trigger({
        notifications: this.notifications,
        loading: false
      });
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
  },

  onDeleteNotification(notificationId) {
    SettingsApi.deleteNotification(this.params, notificationId, (resp) => {
      this.notifications = resp;
      this.trigger({
        notifications: this.notifications,
        loading: false
      });
    });
  },

  onTriggerInterProjectBuilds(params) {
    this.triggerInterProjectBuilds = !this.triggerInterProjectBuilds;
    const data = {
      branchId: params.branchId,
      triggerInterProjectBuilds: this.triggerInterProjectBuilds,
      interProjectBuildOptIn: this.interProjectBuildOptIn
    };

    SettingsApi.updateSettings(params, data, (resp) => {
      this.triggerInterProjectBuilds = resp.triggerInterProjectBuilds;
      this.interProjectBuildOptIn = resp.interProjectBuildOptIn;
      this.trigger({
        triggerInterProjectBuilds: this.triggerInterProjectBuilds,
        interProjectBuildOptIn: this.interProjectBuildOptIn,
        loadingSettings: false
      });
    });
  },

  onInterProjectBuildOptIn(params) {
    this.interProjectBuildOptIn = !this.interProjectBuildOptIn;
    const data = {
      branchId: params.branchId,
      triggerInterProjectBuilds: this.triggerInterProjectBuilds,
      interProjectBuildOptIn: this.interProjectBuildOptIn
    };

    SettingsApi.updateSettings(params, data, (resp) => {
      this.triggerInterProjectBuilds = resp.triggerInterProjectBuilds;
      this.interProjectBuildOptIn = resp.interProjectBuildOptIn;
      this.trigger({
        triggerInterProjectBuilds: this.triggerInterProjectBuilds,
        interProjectBuildOptIn: this.interProjectBuildOptIn,
        loadingSettings: false
      });
    });
  }
});

export default SettingsStore;
