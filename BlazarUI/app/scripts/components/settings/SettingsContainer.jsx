/*global config*/
import React, {Component, PropTypes} from 'react';
import {bindAll} from 'underscore';
import {Link} from 'react-router';

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

  renderHeadline() {
    const branchUrl = `/builds/branch/${this.props.params.branchId}`;

    return (
      <Headline className='notifications__page-headline'>
        <Icon type="fa" name="wrench" classNames="headline-icon" />
        <span>Settings</span>
        <HeadlineDetail>
          <Link style={{cursor: 'pointer'}} to={branchUrl}>Back to branch</Link>
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
