import React, {Component, PropTypes} from 'react';
import {findWhere, bindAll} from 'underscore';
import Toggle from 'react-toggle';

import Icon from '../shared/Icon.jsx';

import SettingsActions from '../../actions/settingsActions';

class NotificationsList extends Component {
  constructor(props) {
    super(props);
    bindAll(this, 'toggleNotificationSetting');
  }

  toggleNotificationSetting(event) {
    const {notifications, channel} = this.props;

    const channelNotifications = findWhere(notifications, {channelName: channel});
    channelNotifications[event.target.id] = !channelNotifications[event.target.id];

    SettingsActions.updateNotification(channelNotifications);
  }

  renderDefault() {
    return (
      <div className="notifications__list--empty">
        <Icon type="fa" name="bell-slash-o" />
        <p>
          No channels configured for notifications.
        </p>
        <p>
          Add your first one here.
        </p>
      </div>
    );
  }

  renderNotificationsList() {
    const {notifications, channel} = this.props;

    if (channel === undefined || !notifications.length) {
      return this.renderDefault();
    }

    const channelNotifications = findWhere(notifications, {channelName: channel});

    if (!channelNotifications) {
      return null;
    }

    return (
      <div>
        <div className="notifications__setting">
          <div className="notifications__setting-title">
            <span>
              On Failure
            </span>
          </div>
          <div className="notifications__setting-description">
            <span>
              Send a notification every time this build fails
            </span>
          </div>
          <div className="notifications__setting-toggle">
            <Toggle
              id="onFail"
              onChange={this.toggleNotificationSetting}
              checked={channelNotifications.onFail}
            />
          </div>
        </div>
        <div className="notifications__setting">
          <div className="notifications__setting-title">
            <span>
              On Success
            </span>
          </div>
          <div className="notifications__setting-description">
            <span>
              Send a notification every time this build succeeds
            </span>
          </div>
          <div className="notifications__setting-toggle">
            <Toggle
              id="onFinish"
              onChange={this.toggleNotificationSetting}
              checked={channelNotifications.onFinish}
            />
          </div>
        </div>
        <div className="notifications__setting">
          <div className="notifications__setting-title">
            <span>
              On Status Change
            </span>
          </div>
          <div className="notifications__setting-description">
            <span>
              Send a notification when this build breaks or recovers
            </span>
          </div>
          <div className="notifications__setting-toggle">
            <Toggle
              id="onChange"
              onChange={this.toggleNotificationSetting}
              checked={channelNotifications.onChange}
            />
          </div>
        </div>
      </div>
    );
  }

  render() {
    return (
      <div className="notifications__list">
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
