import React, { Component, PropTypes } from 'react';
import classNames from 'classnames';
import { Link } from 'react-router';
import { buildResultIcon } from '../Helpers';
import moment from 'moment';
import humanizeDuration from 'humanize-duration';

import Card from './Card.jsx';
import Icon from '../shared/Icon.jsx';
import BuildStates from '../../constants/BuildStates';
import Loader from '../shared/Loader.jsx';

class RepoBranchCard extends Card {

  getClassNames() {
    return classNames([
      this.getBaseClassNames(),
      'card-stack__card--repo-branch'
    ]);
  }

  getBuildToDisplay() {
    const {item} = this.props;

    if (item.get('inProgressBuild') !== undefined) {
      return item.get('inProgressBuild');
    }

    return item.get('lastBuild');
  }

  renderRepoLink() {
    const {item} = this.props;
    const gitInfo = item.get('gitInfo');

    return (
      <Link to={gitInfo.get('blazarBranchPath')}>
        {gitInfo.get('repository')}
      </Link>
    );
  }

  renderBuildNumberLink() {
    const {item} = this.props;
    const build = this.getBuildToDisplay();
    const gitInfo = item.get('gitInfo');
    
    return (
      <Link className='repo-branch-card__build-number' to={build.get('blazarPath')}>
        #{build.get('buildNumber')}
      </Link>
    );
  }

  renderModuleRow(module, i) {
    const state = module.state;
    let statusText;
    let durationText;
    let statusClass;
    let extraClass = '';

    if (state === BuildStates.IN_PROGRESS) {
      statusText = 'is building ...';
      durationText = `Started ${moment(module.startTimestamp).fromNow()}`;
      statusClass = 'repo-branch-card__expanded-status-symbol--IN-PROGRESS';
    }

    else if (state === BuildStates.SUCCEEDED) {
      statusText = 'built successfully.';
      durationText = `Finished in ${humanizeDuration(module.endTimestamp - module.startTimestamp, {round: true})}`;
      statusClass = 'repo-branch-card__expanded-status-symbol--SUCCEEDED';
    }

    else if (state === BuildStates.FAILED) {
      statusText = 'build failed.';
      durationText = `Finished in ${humanizeDuration(module.endTimestamp - module.startTimestamp, {round: true})}`;
      statusClass = 'repo-branch-card__expanded-status-symbol--FAILED';
    }

    else if (state === BuildStates.CANCELLED) {
      statusText = 'was cancelled.';
      durationText = `Cancelled after ${humanizeDuration(module.endTimestamp - module.startTimestamp, {round: true})}`;
      statusClass = 'repo-branch-card__expanded-status-symbol--CANCELLED';
      extraClass = ' repo-branch-card__expanded-module-row--no-log';
    }

    else if (state === BuildStates.SKIPPED) {
      statusText = 'was skipped.';
      statusClass = 'repo-branch-card__expanded-status-symbol--SKIPPED';
      extraClass = ' repo-branch-card__expanded-module-row--no-log';
    }

    else if (state === BuildStates.UNSTABLE) {
      statusText = 'build is unstable.';
      statusClass = 'repo-branch-card__expanded-status-symbol--UNSTABLE';
      extraClass = ' repo-branch-card__expanded-module-row--no-log';
    }

    const rowClassName = `repo-branch-card__expanded-module-row${extraClass}`;

    const innerContent = (
      <div className={rowClassName}>
        <span className='repo-branch-card__expanded-status'>
          <span className={statusClass} /> {module.name} {statusText}
        </span>
        <span className='repo-branch-card__expanded-timestamp'>
          {durationText}
        </span>
      </div>
    );

    if (state === BuildStates.SKIPPED || state === BuildStates.CANCELLED) {
      return (
        <div key={i}>
          {innerContent}
        </div>
      );
    }

    return (
      <div key={i}>
        <Link to={module.blazarPath}>
          {innerContent}
        </Link>
      </div>
    );
  }

  renderDetailsV2() {
    const {item, moduleBuildsList, expanded, loading} = this.props;
    const build = this.getBuildToDisplay();

    if (!expanded || loading) {
      return (
        <div className='card-stack__expanded hiddenn'>
          <div className='card-stack__expanded-module-rows hiddenn' />
        </div>
      );
    }

    const moduleRowNodes = moduleBuildsList.map(this.renderModuleRow);

    return (
      <div className='card-stack__expanded'>
        <div className='card-stack__expanded-header'>
          <span>Build #{build.get('buildNumber')} was started manually</span>
        </div>
        <div className='card-stack__expanded-module-rows'>
          {moduleRowNodes}
        </div>
      </div>
    );
  }

  renderInfo() {
    const {item} = this.props;
    const gitInfo = item.get('gitInfo');

    return (
      <div className='repo-branch-card__info'>
        <div className='repo-branch-card__repo'>
          {this.renderRepoLink()}
        </div>
        <div className='repo-branch-card__branch-and-build'>
          <span className='repo-branch-card__branch'>
            {gitInfo.get('branch')}
          </span>
          <span> is on build </span>
          {this.renderBuildNumberLink()}
        </div>
      </div>
    );
  }

  renderBuildAuthorMaybe() {
    const {item} = this.props;
    const trigger = this.getBuildToDisplay().get('buildTrigger');
    const author = trigger.get('id');
    console.log(item.toJS());

    if (trigger.get('type') !== 'MANUAL' || author === 'unknown') {
      return null;
    }

    return (
      <span>
        &nbsp;by <span className='repo-branch-card__author'>{author}</span>
      </span>
    );
  }

  renderLastBuild() {
    const {item} = this.props;
    const build = this.getBuildToDisplay();

    return (
      <div className='repo-branch-card__last-build'>
        <span className='repo-branch-card__last-build-time'>
          {moment(build.get('startTimestamp')).fromNow()}
        </span>
        {this.renderBuildAuthorMaybe()}
      </div>
    );
  }

  renderTriggeredBy() {
    const {item} = this.props;
    const build = this.getBuildToDisplay();
    let buildTriggerMessage;

    if (build.get('buildTrigger').get('type') === 'MANUAL') {
      buildTriggerMessage = 'Triggered by user';
    }

    else {
      buildTriggerMessage = 'Triggered by code push';
    }

    return (
      <div className='repo-branch-card__details'>
        <div className='repo-branch-card__triggered-by'>
          <span>{buildTriggerMessage}</span>
        </div>
      </div>
    );
  }

  renderStatus() {
    const {item} = this.props;
    const build = this.getBuildToDisplay();
    const classWithModifier = `repo-branch-card__status-banner--${[BuildStates.FAILED, BuildStates.UNSTABLE].indexOf(build.get('state')) !== -1 ? 'FAILED' : 'SUCCESS'}`;

    return (
      <div className='repo-branch-card__status'>
        <div className={classWithModifier}>
          <span />
        </div>
      </div>
    );
  }

  render() {
    const {item} = this.props;
    const build = this.getBuildToDisplay();
    const gitInfo = item.get('gitInfo');

    return (
      <div className={this.getClassNames()}>
        <div onClick={this.props.onClick} className='card-stack__card-main'>
          {this.renderInfo()}
          {this.renderLastBuild()}
          {this.renderTriggeredBy()}
          {this.renderStatus()}
        </div>
        {this.renderDetailsV2()}
      </div>
    );
  }
}

RepoBranchCard.propTypes = {
  item: PropTypes.object.isRequired,
  loading: PropTypes.bool.isRequired,
  moduleBuildsList: PropTypes.array
};

export default RepoBranchCard;