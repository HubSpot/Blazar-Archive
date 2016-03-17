import React, {Component, PropTypes} from 'react';

class NotificationsChannel extends Component {
  
  constructor() {

  }

  render() {
    return (
      <div className='notifications__channel'>
        Channel: #{this.props.channel}
      </div>
    );
  }
}

NotificationsChannel.propTypes = {
  channel: PropTypes.string.isRequired
};

export default NotificationsChannel;