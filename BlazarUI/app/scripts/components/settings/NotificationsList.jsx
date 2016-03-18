import React, {Component, PropTypes} from 'react';
import {findWhere, bindAll} from 'underscore';
import Toggle from 'react-toggle';

import SettingsActions from '../../actions/settingsActions';

class NotificationsList extends Component {
  constructor() {
    bindAll(this, 'toggleNotificationSetting');
  }

  toggleNotificationSetting(event) {
    const {notifications, channel} = this.props;

    let channelNotifications = findWhere(notifications, {channelName: channel});
    channelNotifications[event.target.id] = !channelNotifications[event.target.id];

    console.log("about to try and update: ", channelNotifications);

    SettingsActions.updateNotification(channelNotifications);
  }

  renderNotificationsList() {
    const {notifications, channel} = this.props;

    if (channel === undefined) {
      return null;
    }

    const channelNotifications = findWhere(notifications, {channelName: channel});

    return (
      <div>
        <div className='notifications__setting'>
          <div className='notifications__setting-title'>
            <span>
              Build Failure
            </span>
          </div>
          <div className='notifications__setting-description'>
            <span>
              Send a notification every time this build fails
            </span>
          </div>
          <div className='notifications__setting-toggle'>
            <Toggle
              id='onFail'
              onChange={this.toggleNotificationSetting}
              defaultChecked={channelNotifications.onFail}
            />
          </div>
        </div>
        <div className='notifications__setting'>
          <div className='notifications__setting-title'>
            <span>
              Build Success
            </span>
          </div>
          <div className='notifications__setting-description'>
            <span>
              Send a notification every time this build succeeds
            </span>
          </div>
          <div className='notifications__setting-toggle'>
            <Toggle
              id='onFinish'
              onChange={this.toggleNotificationSetting}
              defaultChecked={channelNotifications.onFinish}
            />
          </div>
        </div>
        <div className='notifications__setting'>
          <div className='notifications__setting-title'>
            <span>
              Build Status Change
            </span>
          </div>
          <div className='notifications__setting-description'>
            <span>
              Send a notification when this build breaks or recovers
            </span>
          </div>
          <div className='notifications__setting-toggle'>
            <Toggle
              id='onChange'
              onChange={this.toggleNotificationSetting}
              defaultChecked={channelNotifications.onChange}
            />
          </div>
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