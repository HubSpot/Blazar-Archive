import React, {Component, PropTypes} from 'react';

class NotificationsHeadline extends Component {

  render() {
    return (
      <div className='notifications__headline'>
        <span className='notifications__headline-title'>
          Build Notifications
        </span>
        <p className='notifications-headline-details'>
          This is where we describe build notifications. Yeah! YEAH
        </p>
      </div>
    );
  }
}

NotificationsHeadline.propTypes = {

};

export default NotificationsHeadline;