import React, {Component, PropTypes} from 'react';

import Icon from '../shared/Icon.jsx';

let initialState = {
  deleteConfirmation: false
}

class NotificationsChannel extends Component {
  
  constructor() {
    this.state = initialState;
  }

  deleteNotification() {
    this.props.onDelete();
    this.hideDeleteConfirmation();
  }

  hideDeleteConfirmation() {
    this.setState({
      deleteConfirmation: false
    })
  }

  onClickTrashCan() {
    this.setState({
      deleteConfirmation: true
    });
  }

  renderDeleteConfirmation() {
    if (!this.state.deleteConfirmation) {
      return null;
    }

    return (
      <div className='notifications__delete-select'>
        Delete? 
        <div onClick={this.deleteNotification.bind(this)}>Yes</div>
        <div onClick={this.hideDeleteConfirmation.bind(this)}>No</div>
      </div>
    );
  }

  render() {
    const extraClassName = this.props.isSelected ? ' selected' : '';
    const finalClassName = `notifications__channel${extraClassName}`;

    return (
      <div className={finalClassName} onClick={this.props.onClick.bind(this, this.props.channel)}>
        <div className='notifications__channel-wrapper'>
          # {this.props.channel}
        </div>
        <div className='notifications__delete' onClick={this.onClickTrashCan.bind(this)}>
          <Icon type='fa' name='trash' />
        </div>
        {this.renderDeleteConfirmation()}
      </div>
    );
  }
}

NotificationsChannel.propTypes = {
  channel: PropTypes.string.isRequired,
  isSelected: PropTypes.bool.isRequired,
  onClick: PropTypes.func.isRequired,
  onDelete: PropTypes.func.isRequired
};

export default NotificationsChannel;