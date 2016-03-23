import React, {Component, PropTypes} from 'react';
import {bindAll} from 'underscore';
import Select from 'react-select';

import NotificationsChannel from './NotificationsChannel.jsx';

import SettingsActions from '../../actions/settingsActions';

let initialState = {
  channelDeleted: undefined
};

class NotificationsChannels extends Component {
  
  constructor() {
    this.state = initialState;

    bindAll(this, 'handleSlackChannelPicked');
  }

  handleSlackChannelPicked(channelName) {
    SettingsActions.addNotification(channelName);
    this.props.onSelectedNewChannel();
  }

  deleteNotification(notificationId, channelName) {
    SettingsActions.deleteNotification(notificationId);
    this.props.onChannelDelete(channelName);

    this.setState({
      channelDeleted: channelName
    });

    setTimeout(() => {
      this.setState({
        channelDeleted: undefined
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
          onDelete={this.deleteNotification.bind(this, notification.id, notification.channelName)}
          key={i} />
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
      <Select
        placeholder='Select a Slack channel'
        className='slack-channel-input'
        name="slackChannel"
        options={slackChannels}
        onChange={this.handleSlackChannelPicked}
        />
    );
  }

  renderToast() {
    if (this.state.channelDeleted === undefined) {
      return null;
    }

    return (
      <div className='notifications__delete-toast'>
        <div className='notifications__delete-toast-inner'>
          <span>Notification channel removed</span>
          <p>You will no longer receive notifications in <strong>#{this.state.channelDeleted}</strong></p>
        </div>
      </div>
    );
  }

  render() {
    return (
      <div>
        <div className='notifications__channels-headline'>
          <span>
            Channels
          </span>
        </div>
        <div className='notifications__channels'>
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