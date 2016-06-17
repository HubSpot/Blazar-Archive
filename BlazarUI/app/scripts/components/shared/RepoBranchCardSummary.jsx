import React, {Component, PropTypes} from 'react';
import moment from 'moment';
import {Link} from 'react-router';
import BuildStates from '../../constants/BuildStates';
import Sha from './Sha.jsx';

class RepoBranchCardSummary extends Component {

  getBuildToDisplay() {
    const {item} = this.props;

    if (item.get('inProgressBuild') !== undefined) {
      return item.get('inProgressBuild');
    }

    return item.get('lastBuild');
  }

  getBuildTime(startTimestamp) {
    const timestampText = moment(startTimestamp).fromNow();

    if (timestampText === 'a day ago') {
      return 'yesterday';
    }

    return timestampText;
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

  renderRepoLink() {
    const {item} = this.props;
    const gitInfo = item.get('gitInfo');

    return (
      <Link to={gitInfo.get('blazarRepositoryPath')}>
        {gitInfo.get('repository')}
      </Link>
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
      <div className='repo-branch-card__triggered-by'>
        <span>{buildTriggerMessage} {sha ? '(' : ''}{sha}{sha ? ')' : ''}</span>
      </div>
    );
  }

  render() {
    return (
      <div className='repo-branch-card__summary'>
        {this.renderBuildAndStatus()}
        <div className='repo-branch-card__main'>
          {this.renderInfo()}
          {this.renderLastBuild()}
          {this.renderTriggeredBy()}
        </div>
      </div>
    );
  }
}

RepoBranchCardSummary.propTypes = {
  item: PropTypes.object.isRequired
};

export default RepoBranchCardSummary;
