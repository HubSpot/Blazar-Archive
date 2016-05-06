/*global config*/
import React, {Component, PropTypes} from 'react';

class NotificationsHeadline extends Component {

  renderSlackBotMessageMaybe() {
    if (!config.slackBotName) {
      return null;
    }

    return (
      <p className='notifications-headline-details'>
        To get notifications invite @{config.slackBotName} so he can post in your channel.
      </p>
    );
  }

  render() {
    return (
      <div className='notifications__headline'>
        <span className='notifications__headline-title'>
          Build Notifications
        </span>
        <p className='notifications-headline-details'>
          Blazar allows you to send notifications about your builds to multiple Slack channels.
        </p>
        {this.renderSlackBotMessageMaybe()}
      </div>
    );
  }
}

NotificationsHeadline.propTypes = {

};

export default NotificationsHeadline;
