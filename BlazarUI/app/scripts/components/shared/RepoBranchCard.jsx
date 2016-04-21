import React, { Component, PropTypes } from 'react';
import classNames from 'classnames';
import { Link } from 'react-router';
import { buildResultIcon, timestampFormatted } from '../Helpers';

import Card from './Card.jsx';
import Icon from '../shared/Icon.jsx';
import BuildStates from '../../constants/BuildStates';
import Loader from '../shared/Loader.jsx';

class RepoBranchCard extends Card {

  getClassNames() {
    return classNames([
      'card-stack__card',
      'card-stack__card--repo-branch',
      this.props.expanded ? 'card-stack__card--expanded' : '',
      this.props.first ? 'card-stack__card--first' : '',
      this.props.last ? 'card-stack__card--last' : '',
      this.props.belowExpanded ? 'card-stack__card--below-expanded' : ''
    ]);
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
    const build = item.get('inProgressBuild') !== undefined ? item.get('inProgressBuild') : item.get('lastBuild');
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

    return moduleBuilds.map((build, key) => {
      return (
        <div key={key} className='card-stack__card-failed-module'>
          {build.name}
        </div>
      );
    });
  }

  renderDetails() {
    const {moduleBuilds} = this.props;

    if (!this.props.expanded) {
      return;
    }

    else if (this.props.loading) {
      return (
        <div className='card-stack__card-details'>
          <Loader roomy={true} />
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
      <div onClick={this.props.onClick} className={this.getClassNames()}>
        <div className='card-stack__card-main'>
          <div className='card-stack__card-status'>
            {buildResultIcon(build.get('state'))}
          </div>
          <div className='card-stack__card-repo-and-branch'>
            <span className='card-stack__card-repo'>
              {this.renderRepoLink()}
            </span>
            <span className='card-stack__card-branch'>
              {gitInfo.get('branch')}
            </span>
          </div>
          <div className='card-stack__card-build-number'>
            <span>{this.renderBuildNumberLink()}</span>
          </div>
          <div className='card-stack__card-timestamp'>
            <span>{timestampFormatted(build.get('startTimestamp'))}</span>
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