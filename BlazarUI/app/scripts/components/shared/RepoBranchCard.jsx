import React, { Component, PropTypes } from 'react';
import classNames from 'classnames';
import { Link } from 'react-router';
import { buildResultIcon } from '../Helpers';
import moment from 'moment';
import humanizeDuration from 'humanize-duration';

import Card from './Card.jsx';
import ModuleRow from './ModuleRow.jsx';
import BuildStates from '../../constants/BuildStates';
import Loader from '../shared/Loader.jsx';
import Sha from '../shared/Sha.jsx';

class RepoBranchCard extends Card {

  getClassNames() {
    const build = this.getBuildToDisplay();
    const isFailingBuild = build.get('state') === BuildStates.FAILED
      || build.get('state') === BuildStates.UNSTABLE;

    return classNames([
      this.getBaseClassNames(),
      'card-stack__card--repo-branch', {
        'repo-branch-card--failed': isFailingBuild
      }
    ]);
  }

  sortModules() {
    const {moduleBuildsList} = this.props;
    const build = this.getBuildToDisplay().toJS();

    if (!build || !build.dependencyGraph || !build.dependencyGraph.topologicalSort) {
      return moduleBuildsList;
    }

    const topologicalSort = build.dependencyGraph.topologicalSort;

    return moduleBuildsList.sort((a, b) => {
      let indexA = topologicalSort.indexOf(a.moduleId);
      let indexB = topologicalSort.indexOf(b.moduleId);

      if (indexA < indexB) {
        return -1;
      }

      return 1;
    });
  }

  getBuildTime(startTimestamp) {
    const timestampText = moment(startTimestamp).fromNow();

    if (timestampText === 'a day ago') {
      return 'yesterday';
    }

    return timestampText;
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
      <Link to={gitInfo.get('blazarRepositoryPath')}>
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

    if (!previousCommit) {
      return null;
    }

    const currentCommit = commitInfo.get('current');
    const commitUrl = previousCommit.get('url').replace('/commit/', '/compare/') + '...' + currentCommit.get('id');

    return (
      <a href={commitUrl}>
        compare
      </a>
    );
  }

  renderModuleRows(modules) {
    return modules.map((module, i) => {
      return (
        <ModuleRow
          module={module}
          key={i} />
      );
    });
  }

  renderDetails() {
    const {item, moduleBuildsList, expanded, loading} = this.props;
    const build = this.getBuildToDisplay();

    if (!expanded) {
      return (
        <div className='card-stack__expanded collapsed'>
          <div className='card-stack__expanded-module-rows collapsed' />
        </div>
      );
    }

    else if (loading) {
      return (
        <div className='card-stack__expanded'>
          <Loader align='center' />
        </div>
      );
    }

    let buildTriggerMessage;
    let detailedTriggerMessage;

    if (build.get('buildTrigger') && build.get('buildTrigger').get('type') === 'MANUAL') {
      buildTriggerMessage = 'manually';
      const buildTime = this.getBuildTime(build.get('startTimestamp'));
      const author = build.get('buildTrigger').get('id');
      detailedTriggerMessage = `Triggered ${buildTime}${author === 'unknown' ? '' : ` by ${author}`}`;
    }

    else {
      buildTriggerMessage = 'automatically by a code push';
      detailedTriggerMessage = this.buildCompareLink();
    }

    return (
      <div className='card-stack__expanded'>
        <div className='repo-branch-card__expanded-header'>
          <span>Build {this.renderBuildNumberLink()} was started {buildTriggerMessage}</span>
          <span className='repo-branch-card__expanded-author'>{detailedTriggerMessage}</span>
        </div>
        <div className='repo-branch-card__expanded-module-rows'>
          {this.renderModuleRows(this.sortModules(moduleBuildsList))}
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
            <Link to={gitInfo.get('blazarBranchPath')}>
              {gitInfo.get('branch')}
            </Link>
          </span>
        </div>
      </div>
    );
  }

  renderBuildAuthorMaybe() {
    const {item} = this.props;
    const trigger = this.getBuildToDisplay().get('buildTrigger');
    const author = trigger.get('id');

    if (trigger && trigger.get('type') !== 'MANUAL' || author === 'unknown') {
      return null;
    }

    return (
      <span>
        Triggered by <span className='repo-branch-card__author'>{author}</span>
      </span>
    );
  }

  renderLastBuild() {
    const {item} = this.props;
    const build = this.getBuildToDisplay();
    let timestamp;

    if (build.get('state') === BuildStates.IN_PROGRESS) {
      timestamp = 'In Progress';
    }

    else {
      timestamp = this.getBuildTime(build.get('startTimestamp'));
    }

    return (
      <div className='repo-branch-card__last-build'>
        <span className='repo-branch-card__last-build-time'>
          {timestamp}
        </span>
      </div>
    );
  }

  renderTriggeredBy() {
    const {item} = this.props;
    const build = this.getBuildToDisplay();
    let buildTriggerMessage;
    let sha;

    if (build.get('buildTrigger') && build.get('buildTrigger').get('type') === 'MANUAL') {
      const buildAuthor = this.renderBuildAuthorMaybe();
      if (!buildAuthor) {
        buildTriggerMessage = 'Triggered by user';
      }

      else {
        buildTriggerMessage = buildAuthor;
      }
    }

    else {
      buildTriggerMessage = 'Triggered by code push';
      const commitInfo = build.get('commitInfo');

      if (build.get('sha') && commitInfo) {
        const gitInfo = {
          host: commitInfo.get('host'),
          organization: commitInfo.get('organization'),
          repository: commitInfo.get('repository')
        };

        sha = (<Sha gitInfo={gitInfo} build={build.toJS()} />);
      }
    }

    return (
      <div className='repo-branch-card__details'>
        <div className='repo-branch-card__triggered-by'>
          <span>{buildTriggerMessage} {sha ? '(' : ''}{sha}{sha ? ')' : ''}</span>
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
        {this.renderDetails()}
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