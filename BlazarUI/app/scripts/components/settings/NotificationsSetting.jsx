import React, {Component, PropTypes} from 'react';

class NotificationsSetting extends Component {
  constructor() {

  }

  render() {
    return (
      <div className='notifications__setting'>

      </div>
    );
  }
}

NotificationsSetting.propTypes = {
  name: PropTypes.string.isRequired,
  value: PropTypes.bool.isRequired
}

export default NotificationsSetting;