import React, {Component, PropTypes} from 'react';
import {Button} from 'react-bootstrap';
import {bindAll} from 'underscore';

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
    if (this.state.selectedChannel !== undefined) {
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
  }

  onSelectedNewChannel() {
    this.setState({
      addingNewChannel: false
    });
  }

  onChannelDelete(channelName) {
    if (this.state.selectedChannel === channelName) {
      this.setState({
        selectedChannel: undefined
      });
    }
  }

  render() {
    return (
      <div className='notifications'>
        <NotificationsHeadline />
        <UIGrid className='notifications__grid'>
          <UIGridItem size={4}>
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
        <Button className='notifications__channel-button' onClick={this.onButtonClick} bsStyle='primary'>Add New Channel</Button>
      </div>
    );
  }
}

Notifications.propTypes = {
  notifications: PropTypes.array.isRequired,
  slackChannels: PropTypes.array.isRequired
};

export default Notifications;