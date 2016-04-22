import React, { Component, PropTypes } from 'react';
import classNames from 'classnames';
import { Link } from 'react-router';
import { buildResultIcon } from '../Helpers';
import moment from 'moment';

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
      <Link to={build.get('blazarPath')}>
        #{build.get('buildNumber')}
      </Link>
    );
  }

  renderFailedModules(moduleBuilds) {
    if (moduleBuilds.length === 0) {
      return (
        <div>No failing modules!</div>
      );
    }

    const failedModuleNodes = moduleBuilds.map((build, key) => {
      return (
        <div key={key} className='repo-branch-card__failed-module'>
          <Link to={build.blazarPath}>
            {build.name}
          </Link>
        </div>
      );
    });

    return (
      <div className='repo-branch-card__failed-modules-wrapper'>
        <span className='repo-branch-card__failed-modules-title'>
          These modules failed:
        </span>
        <br />
        {failedModuleNodes}
      </div>
    );
  }

  renderDetails() {
    const {moduleBuilds} = this.props;

    if (!this.props.expanded) {
      return;
    }

    else if (this.props.loading) {
      return (
        <div className='card-stack__card-details'>
          <Loader align='center' roomy={true} />
        </div>
      );
    }

    const failingModuleBuilds = moduleBuilds.filter((build) => {
      return build.state === BuildStates.FAILED;
    });

    return (
      <div className='card-stack__card-details'>
        {this.renderFailedModules(failingModuleBuilds)}
      </div>
    );
  }

  render() {
    const {item} = this.props;
    const build = item.get('inProgressBuild') !== undefined ? item.get('inProgressBuild') : item.get('lastBuild');
    const gitInfo = item.get('gitInfo');

    return (
      <div className={this.getClassNames()}>
        <div onClick={this.props.onClick} className='card-stack__card-main'>
          <div className='repo-branch-card__repo-and-branch'>
            <span className='card-stack__card-repo'>
              {this.renderRepoLink()}
            </span>
            <span className='repo-branch-card__branch'>
              {gitInfo.get('branch')}
            </span>
          </div>
          <div className='repo-branch-card__build-number'>
            <span>{this.renderBuildNumberLink()}</span>
          </div>
          <div className='repo-branch-card__timestamp'>
            <span>{moment(build.get('startTimestamp')).fromNow()}</span>
          </div>
        </div>
        {this.renderDetails()}
      </div>
    );
  }
}

RepoBranchCard.propTypes = {
  item: PropTypes.object.isRequired,
  loading: PropTypes.bool.isRequired
};

export default RepoBranchCard;