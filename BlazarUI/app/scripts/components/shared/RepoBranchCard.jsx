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

  buildCompareLink() {
    const commitInfo = this.getBuildToDisplay().get('commitInfo');
    const previousCommit = commitInfo.get('previous');
    const currentCommit = commitInfo.get('current');

    return previousCommit.get('url').replace('/commit/', '/compare/') + '...' + currentCommit.get('id');
  }

  renderModuleRow(module, i) {
    const state = module.state;
    let statusText;
    let durationText;
    let extraClass = '';

    if (state === BuildStates.IN_PROGRESS) {
      statusText = 'is building ...';
      durationText = `Started ${moment(module.startTimestamp).fromNow()}`;
    }

    else if (state === BuildStates.SUCCEEDED) {
      statusText = 'built successfully.';
      durationText = `Finished in ${humanizeDuration(module.endTimestamp - module.startTimestamp, {round: true})}`;
    }

    else if (state === BuildStates.FAILED) {
      statusText = 'build failed.';
      durationText = `Finished in ${humanizeDuration(module.endTimestamp - module.startTimestamp, {round: true})}`;
    }

    else if (state === BuildStates.CANCELLED) {
      statusText = 'was cancelled.';
      durationText = `Cancelled after ${humanizeDuration(module.endTimestamp - module.startTimestamp, {round: true})}`;
      extraClass = ' repo-branch-card__expanded-module-row--no-log';
    }

    else if (state === BuildStates.SKIPPED) {
      statusText = 'was skipped.';
      extraClass = ' repo-branch-card__expanded-module-row--no-log';
    }

    else if (state === BuildStates.UNSTABLE) {
      statusText = 'build is unstable.';
      extraClass = ' repo-branch-card__expanded-module-row--no-log';
    }

    const rowClassName = `repo-branch-card__expanded-module-row${extraClass}`;

    const innerContent = (
      <div className={rowClassName}>
        <span className='repo-branch-card__expanded-status'>
          <div className='repo-branch-card__building-icon-link'>
            {buildResultIcon(state)}
          </div> {module.name} {statusText}
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
    let buildTriggerMessage;
    let detailedTriggerMessage;
    console.log(build.toJS());

    if (build.get('buildTrigger').get('type') === 'MANUAL') {
      buildTriggerMessage = 'manually';
      const buildTime = moment(build.get('startTimestamp')).fromNow();
      const author = build.get('buildTrigger').get('id');
      detailedTriggerMessage = `Triggered ${buildTime}${author === 'unknown' ? '' : ` by ${author}`}`;
    }

    else {
      buildTriggerMessage = 'automatically by a code push';
      detailedTriggerMessage = (
        <Link to={this.buildCompareLink()}>
          compare
        </Link>
      );
    }

    return (
      <div className='card-stack__expanded'>
        <div className='card-stack__expanded-header'>
          <span>Build #{build.get('buildNumber')} was started {buildTriggerMessage}</span>
          <span className='card-stack__expanded-author'>{detailedTriggerMessage}</span>
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

  renderBuildAndStatus() {
    const {item} = this.props;
    const build = this.getBuildToDisplay();
    const colorClass = `repo-branch-card__build-and-status repo-branch-card__build-and-status--${build.get('state')}`;

    return (
      <div className={colorClass}>
        {this.renderBuildNumberLink()}
      </div>
    );
  }

  render() {
    const {item} = this.props;
    const build = this.getBuildToDisplay();
    const gitInfo = item.get('gitInfo');

    return (
      <div className={this.getClassNames()}>
        <div className='repo-branch-card__inner-wrapper' onClick={this.props.onClick}>
          {this.renderBuildAndStatus()}
          <div className='card-stack__card-main'>
            {this.renderInfo()}
            {this.renderLastBuild()}
            {this.renderTriggeredBy()}
          </div>
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