import React, {Component, PropTypes} from 'react';
import {bindAll} from 'underscore';

import Notifications from './Notifications.jsx';

import Headline from '../shared/headline/Headline.jsx';
import HeadlineDetail from '../shared/headline/HeadlineDetail.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import PageContainer from '../shared/PageContainer.jsx';
import Icon from '../shared/Icon.jsx';

import SettingsActions from '../../actions/settingsActions';
import SettingsStore from '../../stores/settingsStore';

let initialState = {
  notifications: [],
  slackChannels: [],
  loading: true
};

class SettingsContainer extends Component {

  constructor() {
    this.state = initialState;

    bindAll(this, 'onStatusChange');
  }

  componentDidMount() {
    this.unsubscribeFromSettings = SettingsStore.listen(this.onStatusChange);
    SettingsActions.loadNotifications(this.props.params);
    SettingsActions.loadSlackChannels();
  }

  componentWillReceiveProps(nextprops) {
    this.setState(initialState);
    SettingsActions.loadNotifications(nextProps.params);
  }

  componentWillUnmount() {
    this.unsubscribeFromSettings();
  }

  onStatusChange(state) {
    this.setState(state);
  }

  goBack() {
    window.history.back();
  }

  renderHeadline() {
    return (
      <Headline>
        <Icon type="fa" name="wrench" classNames="headline-icon" />
        <span>Settings</span> <br />
        <HeadlineDetail>
          <a style={{cursor: 'pointer'}} onClick={this.goBack}>Back to branch</a>
        </HeadlineDetail>
      </Headline>
    );
  }

  render() {
    return (
      <PageContainer>
        <UIGrid>
          <UIGridItem size={12}>
            {this.renderHeadline()}
            <Notifications
              notifications={this.state.notifications}
              slackChannels={this.state.slackChannels} />
          </UIGridItem>
        </UIGrid>
      </PageContainer>
    );
  }
}


SettingsContainer.propTypes = {
  params: PropTypes.object.isRequired
};

export default SettingsContainer;
