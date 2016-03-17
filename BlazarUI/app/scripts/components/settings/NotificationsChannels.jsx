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
          key={i} />
      );
    });
  }

  render() {
    return (
      <div className='notifications__channels'>
        {this.renderChannels()}
      </div>
    );
  }
}

NotificationsChannels.propTypes = {
  notifications: PropTypes.array.isRequired
};

export default NotificationsChannels;