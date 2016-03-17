import Reflux from 'reflux';
import SettingsActions from '../actions/settingsActions';
import SettingsApi from '../data/SettingsApi';

const SettingsStore = Reflux.createStore({

  listenables: SettingsActions,

  init() {  
    this.notifications = [];
  },

  getNotifications() {    
    return this.notifications;
  },

  onLoadNotifications(params) {
    SettingsApi.fetchNotifications(params, (resp) => {
      this.notifications = resp;

      this.trigger({
        notifications: this.notifications,
        loading: false
      });
    });
  }

});

export default SettingsStore;
