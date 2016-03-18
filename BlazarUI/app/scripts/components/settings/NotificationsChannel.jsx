import React, {Component, PropTypes} from 'react';

class NotificationsChannel extends Component {
  
  constructor() {

  }

  render() {
    const extraClassName = this.props.isSelected ? ' selected' : '';
    const finalClassName = `notifications__channel${extraClassName}`;

    return (
      <div className={finalClassName} onClick={this.props.onClick.bind(this, this.props.channel)}>
        # {this.props.channel}
      </div>
    );
  }
}

NotificationsChannel.propTypes = {
  channel: PropTypes.string.isRequired,
  isSelected: PropTypes.bool.isRequired,
  onClick: PropTypes.func.isRequired
};

export default NotificationsChannel;