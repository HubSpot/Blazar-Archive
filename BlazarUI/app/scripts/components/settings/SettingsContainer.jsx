import React, {Component, PropTypes} from 'react';
import {bindAll} from 'underscore';

import Notifications from './Notifications.jsx';

import Headline from '../shared/headline/Headline.jsx';
import UIGrid from '../shared/grid/UIGrid.jsx';
import UIGridItem from '../shared/grid/UIGridItem.jsx';
import PageContainer from '../shared/PageContainer.jsx';
import Icon from '../shared/Icon.jsx';
import Loader from '../shared/Loader.jsx';
import SimpleBreadcrumbs from '../shared/SimpleBreadcrumbs.jsx';

import SettingsActions from '../../actions/settingsActions';
import SettingsStore from '../../stores/settingsStore';
import BranchSettings from './BranchSettings.jsx';

import BranchActions from '../../actions/branchActions';
import BranchStore from '../../stores/branchStore';

const initialState = {
  notifications: [],
  slackChannels: [],
  branchInfo: {},
  triggerInterProjectBuilds: false,
  interProjectBuildOptIn: false,
  loading: true,
  loadingBranchInfo: true,
  loadingSettings: true
};

class SettingsContainer extends Component {

  constructor() {
    super();
    this.state = initialState;
    bindAll(this, 'onStatusChange', 'onTriggerInterProjectBuilds', 'onInterProjectBuildOptIn');
  }

  componentDidMount() {
    this.unsubscribeFromSettings = SettingsStore.listen(this.onStatusChange);
    this.unsubscribeFromBranch = BranchStore.listen(this.onStatusChange);
    BranchActions.loadBranchInfo(this.props.params);
    SettingsActions.loadNotifications(this.props.params);
    SettingsActions.loadSettings(this.props.params);
    SettingsActions.loadSlackChannels();
  }

  componentWillReceiveProps(nextProps) {
    this.setState(initialState);
    SettingsActions.loadNotifications(nextProps.params);
    SettingsActions.loadSettings(this.props.params);
  }

  componentWillUnmount() {
    this.unsubscribeFromSettings();
    this.unsubscribeFromBranch();
  }

  onStatusChange(state) {
    this.setState(state);
  }

  buildDocumentTitle() {
    const {branchInfo} = this.state;

    if (!branchInfo) {
      return 'Settings';
    }

    return `Settings | ${branchInfo.repository} - ${branchInfo.branch}`;
  }

  onTriggerInterProjectBuilds() {
    SettingsActions.triggerInterProjectBuilds(this.props.params);
  }

  onInterProjectBuildOptIn() {
    SettingsActions.interProjectBuildOptIn(this.props.params);
  }

  renderHeadline() {
    return (
      <div>
        <SimpleBreadcrumbs repo={true} branch={true} {...this.props} {...this.state} />
        <Headline className="notifications__page-headline">
          <Icon type="fa" name="wrench" classNames="headline-icon" />
          <span>Settings</span>
        </Headline>
      </div>
    );
  }

  renderContent() {
    if (this.state.loading || this.state.loadingBranchInfo || this.state.loadingSettings) {
      return <Loader align="left" />;
    }

    return (
      <UIGrid>
          <UIGridItem size={12}>
          {this.renderHeadline()}
          <BranchSettings
            triggerInterProjectBuilds={this.state.triggerInterProjectBuilds}
            interProjectBuildOptIn={this.state.interProjectBuildOptIn}
            onTriggerInterProjectBuilds={this.onTriggerInterProjectBuilds}
            onInterProjectBuildOptIn={this.onInterProjectBuildOptIn}
          />
          <Notifications
            notifications={this.state.notifications}
            slackChannels={this.state.slackChannels}
          />
        </UIGridItem>
      </UIGrid>
    );
  }

  render() {
    return (
      <PageContainer documentTitle={this.buildDocumentTitle()}>
        {this.renderContent()}
      </PageContainer>
    );
  }
}


SettingsContainer.propTypes = {
  params: PropTypes.object.isRequired
};

export default SettingsContainer;
