import React, {Component} from 'react';

class NotificationsHeadline extends Component {

  renderSlackBotMessageMaybe() {
    if (!window.config.slackBotName) {
      return null;
    }

    return (
      <p className="notifications-headline-details">
        To get notifications, invite @{window.config.slackBotName} so it can post in your channel.
      </p>
    );
  }

  render() {
    return (
      <div className="notifications__headline">
        <span className="notifications__headline-title">
          Build Notifications
        </span>
        <p className="notifications-headline-details">
          Blazar allows you to send notifications about your builds to multiple Slack channels.
        </p>
        {this.renderSlackBotMessageMaybe()}
      </div>
    );
  }
}

export default NotificationsHeadline;
