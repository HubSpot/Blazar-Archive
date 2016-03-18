import React, {Component, PropTypes} from 'react';
import {bindAll} from 'underscore';
import Select from 'react-select';

import NotificationsChannel from './NotificationsChannel.jsx';

import SettingsActions from '../../actions/settingsActions';

class NotificationsChannels extends Component {
  
  constructor() {
    bindAll(this, 'handleSlackChannelPicked');
  }

  handleSlackChannelPicked(channelName) {
    SettingsActions.addNotification(channelName);
    this.props.onSelectedNewChannel();
  }

  renderChannels() {
    return this.props.notifications.map((notification, i) => {
      return (
        <NotificationsChannel
          channel={notification.channelName}
          isSelected={this.props.selectedChannel === notification.channelName}
          onClick={this.props.onChannelClick}
          key={i} />
      );
    });
  }

  renderNewChannel() {
    if (!this.props.addingNewChannel) {
      return null;
    }

    return (
      <Select
        placeholder='Select a Slack channel'
        className='slack-channel-input'
        name="slackChannel"
        options={this.props.slackChannels}
        onChange={this.handleSlackChannelPicked}
        />
    );
  }

  render() {
    return (
      <div className='notifications__channels'>
        <div className='notifications__channels-headline'>
          <span>
            Channels
          </span>
        </div>
        {this.renderChannels()}
        {this.renderNewChannel()}
      </div>
    );
  }
}

NotificationsChannels.propTypes = {
  notifications: PropTypes.array.isRequired,
  slackChannels: PropTypes.array.isRequired,
  onChannelClick: PropTypes.func.isRequired,
  onSelectedNewChannel: PropTypes.func.isRequired,
  addingNewChannel: PropTypes.bool.isRequired,
  selectedChannel: PropTypes.string
};

export default NotificationsChannels;