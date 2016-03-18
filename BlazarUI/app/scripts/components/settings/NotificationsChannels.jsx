import React, {Component, PropTypes} from 'react';

import NotificationsChannel from './NotificationsChannel.jsx';

class NotificationsChannels extends Component {
  
  constructor() {

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

  render() {
    return (
      <div className='notifications__channels'>
        <div className='notifications__channels-headline'>
          <span>
            Channels
          </span>
        </div>
        {this.renderChannels()}
      </div>
    );
  }
}

NotificationsChannels.propTypes = {
  notifications: PropTypes.array.isRequired,
  onChannelClick: PropTypes.func.isRequired,
  selectedChannel: PropTypes.string
};

export default NotificationsChannels;