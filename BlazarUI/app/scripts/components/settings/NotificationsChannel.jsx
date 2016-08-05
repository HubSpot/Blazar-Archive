import React, {Component, PropTypes} from 'react';
import {bindAll} from 'underscore';

import Icon from '../shared/Icon.jsx';

class NotificationsChannel extends Component {

  constructor(props) {
    super(props);

    bindAll(this, 'deleteNotification', 'hideDeleteConfirmation', 'onClickTrashCan');

    this.state = {
      deleteConfirmation: false
    };
  }

  componentWillReceiveProps(nextProps) {
    if (!nextProps.isSelected) {
      this.setState({
        deleteConfirmation: false
      });
    }
  }

  deleteNotification() {
    this.props.onDelete();
    this.hideDeleteConfirmation();
  }

  hideDeleteConfirmation() {
    this.setState({
      deleteConfirmation: false
    });
  }

  onClickTrashCan() {
    if (!this.state.deleteConfirmation) {
      this.setState({
        deleteConfirmation: true
      });
    } else {
      this.hideDeleteConfirmation();
    }
  }

  renderDeleteConfirmation() {
    if (!this.props.isSelected) {
      return null;
    }

    let divClass = 'notifications__delete-select';

    if (!this.state.deleteConfirmation) {
      divClass += ' hidden';
    }

    return (
      <div className={divClass}>
        <span className="notifications__delete-select-question">
          Remove?
        </span>
        <div className="notifications__delete-select-yes" onClick={this.deleteNotification}>
          <span>
            Yes
          </span>
        </div>
        <div className="notifications__delete-select-no" onClick={this.hideDeleteConfirmation}>
          <span>
            Keep
          </span>
        </div>
      </div>
    );
  }

  renderArrow() {
    if (!this.props.isSelected) {
      return null;
    }

    return <div className="notifications__triangle" />;
  }

  renderTrashCan() {
    if (!this.props.isSelected) {
      return null;
    }

    return (
      <div className="notifications__delete" onClick={this.onClickTrashCan}>
        <Icon type="fa" name="trash" />
      </div>
    );
  }

  render() {
    const extraClassName = this.props.isSelected ? ' selected' : '';
    const finalClassName = `notifications__channel${extraClassName}`;

    return (
      <div className={finalClassName} onClick={() => this.props.onClick(this.props.channel)}>
        <div className="notifications__channel-wrapper">
          # {this.props.channel}
        </div>
        {this.renderTrashCan()}
        {this.renderArrow()}
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
