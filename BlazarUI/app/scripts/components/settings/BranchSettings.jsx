import React, {Component, PropTypes} from 'react';
import {bindAll} from 'underscore';
import Checkbox from '../shared/Checkbox.jsx';
import Icon from '../shared/Icon.jsx';
import ReactTooltip from 'react-tooltip';
import SettingsActions from '../../actions/settingsActions';

class BranchSettings extends Component {

  constructor(props) {
    super(props);
    var initialState = {
      triggerInterProjectBuilds: this.props.triggerInterProjectBuilds,
      interProjectBuildOptIn: this.props.interProjectBuildOptIn
    }
    this.state = initialState;
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
      <div>
        <div className="interProjectBuildOptIn-checkbox-wrapper">
          <Checkbox
            label='Build as inter project downstream'
            name='interProjectBuildOptIn-checkbox'
            checked={this.state.interProjectBuildOptIn}
            onCheckboxUpdate={this.updateInterProjectBuildOptIn}
          />
          <a data-tip data-for='interProjectBuildOptInTooltip'>
            <Icon
              type='fa'
              name='question-circle'
              classNames='checkbox-tooltip'
            />
          </a>
          <ReactTooltip
            id='interProjectBuildOptInTooltip'
            place='bottom'
            type='dark'
            effect='solid'>
            Opt in all moudles on this branch to being built by their dependencies
          </ReactTooltip>
        </div>
        <div className="triggerInterProjectBuilds-checkbox-wrapper">
          <Checkbox
            label='Pushes build all downstream modules'
            name='triggerInterProjectBuilds-checkbox'
            checked={this.state.triggerInterProjectBuilds}
            onCheckboxUpdate={this.updateTriggerInterProjectBuilds}
          />
          <a data-tip data-for='triggerInterProjectBuildsTooltip'>
            <Icon
              type='fa'
              name='question-circle'
              classNames='checkbox-tooltip'
            />
          </a>
          <ReactTooltip
            id='triggerInterProjectBuildsTooltip'
            place='bottom'
            type='dark'
            effect='solid'>
            Pushes to this branch trigger builds of all modules that depend on modules in this branch
          </ReactTooltip>
        </div>
      </div>
    );
  }
}

export default BranchSettings;