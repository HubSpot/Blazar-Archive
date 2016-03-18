import React, {Component, PropTypes} from 'react';
import {bindAll} from 'underscore';

import NotificationsHeadline from './NotificationsHeadline.jsx';
import NotificationsChannels from './NotificationsChannels.jsx';
import NotificationsList from './NotificationsList.jsx';

import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';

let initialState = {
  selectedChannel: undefined
};

class Notifications extends Component {

  constructor() {
    this.state = initialState;

    bindAll(this, 'onChannelClick');
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

  render() {
    return (
      <div className='notifications'>
        <NotificationsHeadline />
        <UIGrid className='notifications__grid'>
          <UIGridItem size={4}>
            <NotificationsChannels
              selectedChannel={this.state.selectedChannel}
              onChannelClick={this.onChannelClick}
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
      </div>
    );
  }
}

Notifications.propTypes = {
  notifications: PropTypes.array.isRequired
};

export default Notifications;