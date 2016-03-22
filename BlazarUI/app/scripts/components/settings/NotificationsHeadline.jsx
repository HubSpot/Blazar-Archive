import React, {Component, PropTypes} from 'react';

class NotificationsHeadline extends Component {

  render() {
    return (
      <div className='notifications__headline'>
        <span className='notifications__headline-title'>
          Build Notifications
        </span>
        <p className='notifications-headline-details'>
          Create and modify build notifications.
        </p>
      </div>
    );
  }
}

NotificationsHeadline.propTypes = {

};

export default NotificationsHeadline;