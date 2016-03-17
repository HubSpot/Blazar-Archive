import React, {Component, PropTypes} from 'react';
import {findWhere} from 'underscore';

class NotificationsList extends Component {
  constructor() {

  }

  renderNotificationsList() {
    const {notifications, channel} = this.props;

    if (channel === undefined) {
      return null;
    }

    const channelNotifications = findWhere(notifications, {channelName: channel}).map((notification) => {
      return {
        onFinish: notification.onFinish,
        onFail: notification.onFail,
        onChange: notification.onChange,
        onRecover: notification.onRecover
      };
    });

    return (
      <div>
        <div className='notifications__setting'>
          <span className='notifications__setting-title'>
            Build Failure
          </span>
          <span className='notifications__setting-description'>
            Send a notification every time this build fails
          </span>
        </div>
        <div className='notifications__setting'>
          <span className='notifications__setting-title'>
            Build Success
          </span>
          <span className='notifications__setting-description'>
            Send a notification every time this build succeeds
          </span>
        </div>
        <div className='notifications__setting'>
          <span className='notifications__setting-title'>
            Build Status Change
          </span>
          <span className='notifications__setting-description'>
            Send a notification when this build breaks or recovers
          </span>
        </div>
      </div>
    );
  }

  render() {
    return (
      <div className='notifications__list'>
        {this.renderNotificationsList()}
      </div>
    );
  }
}

NotificationsList.propTypes = {
  notifications: PropTypes.array.isRequired,
  channel: PropTypes.string
};

export default NotificationsList;