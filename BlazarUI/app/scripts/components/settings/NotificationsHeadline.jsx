import React, {Component, PropTypes} from 'react';

class NotificationsHeadline extends Component {

  render() {
    return (
      <div className='notifications__headline'>
        <span className='notifications__headline-title'>
          Build Notifications
        </span>
        <p className='notifications-headline-details'>
          Blazar allows you to send notifications about your builds to multiple
          Slack channels.
        </p>
      </div>
    );
  }
}

NotificationsHeadline.propTypes = {

};

export default NotificationsHeadline;
