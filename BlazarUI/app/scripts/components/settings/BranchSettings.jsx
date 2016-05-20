import React, {Component, PropTypes} from 'react';
import {bindAll} from 'underscore';
import Checkbox from '../shared/Checkbox.jsx';
import Icon from '../shared/Icon.jsx';
import ReactTooltip from 'react-tooltip';
import Toggle from 'react-toggle';
import SettingsActions from '../../actions/settingsActions';
import UIGridItem from '../shared/grid/UIGridItem.jsx';


class BranchSettings extends Component {

  constructor(props) {
    super(props);
    bindAll(this, 'updateInterProjectBuildOptIn' ,'updateTriggerInterProjectBuilds');
  }


  updateInterProjectBuildOptIn() {
    this.props.onInterProjectBuildOptIn();
  }

  updateTriggerInterProjectBuilds() {
    this.props.onTriggerInterProjectBuilds();
  }

  render() {
    return (
      <UIGridItem size={12} className='settings__grid main-settings'>
        <div className='notifications__setting'>
          <div className='notifications__setting-title'>
            <span>
              Trigger from upstream builds
            </span>
          </div>
          <div className='notifications__setting-description'>
            <span>
              Opt in all modules on this branch to being built by their dependencies
            </span>
          </div>
          <div className='notifications__setting-toggle'>
            <Toggle
              id='onFail'
              onChange={this.updateInterProjectBuildOptIn}
              checked={this.props.interProjectBuildOptIn}
            />
          </div>
        </div>
        <div className='notifications__setting'>
          <div className='notifications__setting-title'>
            <span>
              Auto build downstream modules
            </span>
          </div>
          <div className='notifications__setting-description'>
            <span>
              Pushes to this branch trigger builds of all modules that depend on modules in this branch
            </span>
          </div>
          <div className='notifications__setting-toggle'>
            <Toggle
              id='onFail'
              onChange={this.updateTriggerInterProjectBuilds}
              checked={this.props.triggerInterProjectBuilds}
            />
          </div>
        </div>
      </UIGridItem>
    );
  }
}

export default BranchSettings;
