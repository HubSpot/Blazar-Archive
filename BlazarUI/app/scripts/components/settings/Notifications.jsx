import React, {Component, PropTypes} from 'react';
import {Button} from 'react-bootstrap';
import {bindAll} from 'underscore';
import $ from 'jquery';
import classNames from 'classnames';

import NotificationsHeadline from './NotificationsHeadline.jsx';
import NotificationsChannels from './NotificationsChannels.jsx';
import NotificationsList from './NotificationsList.jsx';

import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';

let initialState = {
  selectedChannel: undefined,
  addingNewChannel: false
};

class Notifications extends Component {

  constructor() {
    this.state = initialState;

    bindAll(this, 'onChannelClick', 'onButtonClick', 'onSelectedNewChannel', 'onChannelDelete');
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.notifications.length === this.props.notifications.length) {
      return;
    }

    const {notifications} = nextProps;

    if (notifications.length) {
      this.setState({
        selectedChannel: notifications[0].channelName
      });
    }
  }

  getChannels() {
    const {notifications} = this.props;

    return notifications.map((notification) => {
      return notification.channelName;
    });
  }

  onChannelClick(channelName) {
    this.setState({
      selectedChannel: channelName
    });
  }

  onButtonClick() {
    this.setState({
      addingNewChannel: true
    });

    setTimeout(() => {
      $('.Select-input > input').focus();
    }, 50);
  }

  onSelectedNewChannel() {
    this.setState({
      addingNewChannel: false
    });
  }

  onChannelDelete(channelName) {
    // you can use this information
  }

  renderButton() {
    const buttonClasses = classNames([
      'notifications__channel-button',
      this.state.addingNewChannel ? ' disabled' : ''
    ]);

    return (
      <Button 
        className={buttonClasses}
        onClick={this.onButtonClick} 
        bsStyle='primary'>
        Add New Channel
      </Button>
    )
  }

  render() {
    return (
      <div className='notifications'>
        <NotificationsHeadline />
        <UIGrid>
          <UIGridItem size={12}>
            <div className='notifications__channels-headline'>
              <span>
                Channels
              </span>
            </div>
          </UIGridItem>
        </UIGrid>
        <UIGrid className='notifications__grid'>
          <UIGridItem size={4} className='notifications__channel-list'>
            <NotificationsChannels
              selectedChannel={this.state.selectedChannel}
              addingNewChannel={this.state.addingNewChannel}
              onChannelClick={this.onChannelClick}
              onSelectedNewChannel={this.onSelectedNewChannel}
              onChannelDelete={this.onChannelDelete}
              {...this.props}
            />
          </UIGridItem>
          <UIGridItem size={8}>
            <NotificationsList
              channel={this.state.selectedChannel}
              {...this.props}
            />
          </UIGridItem>
        </UIGrid>
        <UIGrid className='notifications__grid channel-button'>
          <UIGridItem size={4}>
            {this.renderButton()}
          </UIGridItem>
          <UIGridItem size={8}>
          </UIGridItem>
        </UIGrid>
      </div>
    );
  }
}

Notifications.propTypes = {
  notifications: PropTypes.array.isRequired,
  slackChannels: PropTypes.array.isRequired
};

export default Notifications;
