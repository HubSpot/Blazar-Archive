import React, {Component, PropTypes} from 'react';

import NotificationsHeadline from './NotificationsHeadline.jsx';
import NotificationsChannels from './NotificationsChannels.jsx';
import NotificationsList from './NotificationsList.jsx';

let initialState = {
  selectedChannel: undefined
};

class Notifications extends Component {

  constructor() {
    this.state = initialState;
  }

  getChannels() {
    const {notifications} = this.props;

    return notifications.map((notification) => {
      return notification.channelName;
    });
  }

  render() {
    return (
      <div className='notifications'>
        <NotificationsHeadline />
        <NotificationsChannels 
          {...this.props}
        />
        <NotificationsList
          channel={this.state.selectedChannel}
          {...this.props}
        />
      </div>
    );
  }
}

Notifications.propTypes = {
  notifications: PropTypes.array.isRequired
};

export default Notifications;