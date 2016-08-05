import React, {Component, PropTypes} from 'react';
import {bindAll} from 'underscore';
import Select from 'react-select';
import moment from 'moment';

import NotificationsChannel from './NotificationsChannel.jsx';

import SettingsActions from '../../actions/settingsActions';

class NotificationsChannels extends Component {

  constructor() {
    this.state = {
      channelDeleted: undefined,
      showToast: false,
      showedToastAt: moment()
    };

    bindAll(this, 'handleSlackChannelPicked');
  }

  componentWillUnmount() {
    if (this.channelTimeout) {
      clearTimeout(this.channelTimeout);
    }

    if (this.toastTimeout) {
      clearTimeout(this.toastTimeout);
    }
  }

  handleSlackChannelPicked(channelName) {
    if (this.channelAlreadyHasNotification(channelName) || channelName === '') {
      return;
    }

    SettingsActions.addNotification(channelName);
    this.props.onSelectedNewChannel();
  }

  channelAlreadyHasNotification(channelName) {
    const existingChannels = this.props.notifications.map((notification) => {
      return notification.channelName;
    });

    return existingChannels.indexOf(channelName) !== -1;
  }

  deleteNotification(notificationId, channelName) {
    SettingsActions.deleteNotification(notificationId);
    this.props.onChannelDelete(channelName);

    this.setState({
      channelDeleted: channelName,
      showToast: true,
      moment: moment()
    });

    this.channelTimeout = setTimeout(() => {
      if (moment() - this.state.moment < 5250) {
        return;
      }

      this.setState({
        channelDeleted: undefined
      });
    }, 5250);

    this.toastTimeout = setTimeout(() => {
      if (moment() - this.state.moment < 5000) {
        return;
      }

      this.setState({
        showToast: false
      });
    }, 5000);
  }

  renderChannels() {
    return this.props.notifications.map((notification, i) => {
      return (
        <NotificationsChannel
          channel={notification.channelName}
          isSelected={this.props.selectedChannel === notification.channelName}
          onClick={this.props.onChannelClick}
          onDelete={() => this.deleteNotification(notification.id, notification.channelName)}
          key={i}
        />
      );
    });
  }

  renderNewChannel() {
    if (!this.props.addingNewChannel) {
      return null;
    }

    const existingChannels = this.props.notifications.map((notification) => {
      return notification.channelName;
    });

    const slackChannels = this.props.slackChannels.filter((slackChannel) => {
      return existingChannels.indexOf(slackChannel.label) === -1;
    });

    return (
      <div className="notifications__new-channel-hashtag">
        <span>#</span>
        <Select
          placeholder="Choose a channel"
          className="slack-channel-input"
          name="slackChannel"
          options={slackChannels}
          onChange={this.handleSlackChannelPicked}
          allowCreate={true}
          addLabelText="{label}"
        />
      </div>
    );
  }

  renderToast() {
    let opacityModifier = '';

    if (this.state.showToast) {
      opacityModifier = ' revealed';
    }

    const classNames = `notifications__delete-toast${opacityModifier}`;

    return (
      <div className={classNames}>
        <div className="notifications__delete-toast-inner">
          <span>Notification channel removed</span>
          <p>You will no longer receive notifications in <strong>#{this.state.channelDeleted}</strong></p>
        </div>
      </div>
    );
  }

  render() {
    return (
      <div className="notifications__channels-container">
        <div className="notifications__channels">
          {this.renderChannels()}
          {this.renderNewChannel()}
          {this.renderToast()}
        </div>
      </div>
    );
  }
}

NotificationsChannels.propTypes = {
  notifications: PropTypes.array.isRequired,
  slackChannels: PropTypes.array.isRequired,
  onChannelClick: PropTypes.func.isRequired,
  onSelectedNewChannel: PropTypes.func.isRequired,
  onChannelDelete: PropTypes.func.isRequired,
  addingNewChannel: PropTypes.bool.isRequired,
  selectedChannel: PropTypes.string
};

export default NotificationsChannels;
