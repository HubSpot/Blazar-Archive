import React, {Component, PropTypes} from 'react';
import {has, contains, bindAll} from 'underscore';
import {humanizeText, timestampFormatted, truncate} from '../Helpers';
import classNames from 'classnames';

import Loader from '../shared/Loader.jsx';
import RepoBuildCancelButton from './RepoBuildCancelButton.jsx';
import Sha from '../shared/Sha.jsx';
import Alert from 'react-bootstrap/lib/Alert';

import Commits from './Commits.jsx';
import InterProjectAlert from './InterProjectAlert.jsx';

import BuildStates from '../../constants/BuildStates';
import FINAL_BUILD_STATES from '../../constants/finalBuildStates';
import {LABELS} from '../constants';

class RepoBuildDetail extends Component {

  constructor() {
    bindAll(this, 'handleResize')
    this.state = {
      windowWidth: window.innerWidth,
      showCommits: false
    }
  }

  componentDidMount() {
    window.addEventListener('resize', this.handleResize);
  }

  componentWillUnmount() {
    window.removeEventListener('resize', this.handleResize);
  }

  getWrapperClassNames(build) {
    return classNames([
      'build-detail',
      'alert',
      `alert-${LABELS[build.state]}`
    ]);
  }

  handleResize(e) {
    this.setState({
      windowWidth: window.innerWidth
    });
  }

  flipShowCommits() {
    this.setState({
      showCommits: !this.state.showCommits
    });
  }

  renderCommits() {
    const {currentRepoBuild} = this.props;
    const build = currentRepoBuild;

    if (!has(build, 'commitInfo')) {
      return null;
    }

    const currentCommit = build.commitInfo.current;
    const newCommits = build.commitInfo.newCommits;
    const commitList = newCommits.length === 0 ? [currentCommit] : newCommits;

    return (
      <Commits
        currentCommit={build.commitInfo.current}
        previousCommit={build.commitInfo.previous}
        commits={commitList}
        showCommits={this.state.showCommits}
        anyNewCommits={newCommits.length > 0}
        flipShowCommits={this.flipShowCommits.bind(this)} />
    );
  }

  renderUnstableMessage() {
    const {currentRepoBuild} = this.props;

    if (currentRepoBuild.state !== BuildStates.UNSTABLE) {
      return null;
    }

    return (
      <span className="build-detail-header__unstable-message">
        This build succeeded, but one or more modules in previous builds have failed and need to be rebuilt.
      </span>
    );
  }

  render() {
    const {currentRepoBuild} = this.props;

    if (this.props.error) {
      return null;
    }

    else if (this.props.loading) {
      return (
        <div className='build-detail'>
          <Loader align='left' roomy={true} />
        </div>
      );
    }

    const build = this.props.currentRepoBuild;

    const gitInfo = {
      host: this.props.params.host,
      organization: this.props.params.org,
      repository: this.props.params.repo
    }

    let endtime, duration;

    let buildDetail = {
      endtime: '',
      duration: '',
      durationPrefix: '',
      buildResult: humanizeText(build.state)
    }

    if (contains(FINAL_BUILD_STATES, build.state)) {
      buildDetail.endtime = timestampFormatted(build.endTimestamp)
      const conjunction = build.state === BuildStates.CANCELLED ? 'after' : 'in';

      buildDetail.duration = `${conjunction} ${build.duration}`;
    }

    else if (build.state === BuildStates.IN_PROGRESS) {
      buildDetail.duration = `started ${timestampFormatted(build.startTimestamp)}`;
    }

    return (
      <div>
        <div className={this.getWrapperClassNames(build)}>
          <p className='build-detail-header__build-state'>
            Build {buildDetail.buildResult}
            <span className='build-detail-header__timestamp'>{buildDetail.duration}</span>
            {this.renderUnstableMessage()}
          </p>
          <RepoBuildCancelButton
            params={this.props.params}
            triggerCancelBuild={this.props.triggerCancelBuild}
            build={build}
          />
        </div>
        <InterProjectAlert
          upAndDownstreamModules={this.props.upAndDownstreamModules}
        />
        {this.renderCommits()}
      </div>
    );
  }

}


RepoBuildDetail.propTypes = {
  loading: PropTypes.bool.isRequired,
  currentRepoBuild: PropTypes.object,
  error: PropTypes.string
};

export default RepoBuildDetail;
