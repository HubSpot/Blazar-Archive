import React, {Component, PropTypes} from 'react';
import {has, contains} from 'underscore';
import {humanizeText, timestampFormatted, truncate} from '../Helpers';
import classNames from 'classnames';

import Sha from '../shared/Sha.jsx';
import Alert from 'react-bootstrap/lib/Alert';
import Icon from '../shared/Icon.jsx';
import { BUILD_ICONS } from '../constants';

import BuildStates from '../../constants/BuildStates';
import FINAL_BUILD_STATES from '../../constants/finalBuildStates';
import {LABELS} from '../constants';

class BuildDetail extends Component {

  constructor() {
    this.handleResize = this.handleResize.bind(this);
    this.state = {
      windowWidth: window.innerWidth
    }
  }

  handleResize(e) {
    this.setState({
      windowWidth: window.innerWidth
    });
  }

  componentDidMount() {
    window.addEventListener('resize', this.handleResize);
  }

  componentWillUnmount() {
    window.removeEventListener('resize', this.handleResize);
  }
  
  getWrapperClassNames() {
    return classNames([
      `build-detail alert alert-${LABELS[this.props.build.build.state]}`
    ]);
  }

  render() {
    const {
      build, 
      gitInfo
    } = this.props.build;

    if (build.state === BuildStates.CANCELLED) {
      return (
        <Alert bsStyle="warning">
          <strong>Build Cancelled</strong> 
        </Alert>
      )
    }

    if (!has(build, 'commitInfo')) {
      return (
        <Alert>No detail available for this build</Alert>
      );
    }

    const currentCommit = build.commitInfo.current
    const newCommits = build.commitInfo.newCommits

    let endtime, duration;

    let buildDetail = {
      endtime: '',
      duration: '',
      durationPrefix: '',
      buildResult: humanizeText(build.state)
    }

    if (contains(FINAL_BUILD_STATES, build.state)) {
      buildDetail.endtime = timestampFormatted(build.endTimestamp)
      buildDetail.duration = (
        <span>in {build.duration}</span>
      );
    }

    if (build.state === BuildStates.IN_PROGRESS) {
      buildDetail.duration = (
        <span> started {timestampFormatted(build.startTimestamp)}</span>
      );
    }

    const shaLink = `https://${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/commit/${build.sha}`;

    return (
      <div className={this.getWrapperClassNames()}>
        
        <div className='build-detail-header'>
          <p className='build-detail-header__build-state'>
            <Icon name={BUILD_ICONS[build.state]} classNames="headline-icon"></Icon>
            Build {buildDetail.buildResult} 
            <span className='build-detail-header__timestamp'>{buildDetail.duration}</span>
          </p>
        </div>  
        
        <div className='build-detail-body'>
          <pre title={currentCommit.message} className='build-detail-body__commit-desc'>{truncate(currentCommit.message, this.state.windowWidth * .08, true)}</pre>
        </div>
        
        <div className='build-detail-footer'> 
          Authored by <strong>{currentCommit.author.name}</strong> on { ' ' }
          {timestampFormatted(currentCommit.timestamp, 'dddd')}, { ' ' }
          {timestampFormatted(currentCommit.timestamp, 'llll')} 
          <span className='build-detail__sha'> 
            commit <Sha gitInfo={gitInfo} build={build} />
          </span>
        </div>
        
      </div>
    );
  }

}


BuildDetail.propTypes = {
  loading: PropTypes.bool.isRequired,
  build: PropTypes.shape({
    build: PropTypes.shape({
      buildNumber: PropTypes.number,
      commitSha: PropTypes.string,
      state: PropTypes.oneOf(['SUCCEEDED', 'FAILED', 'IN_PROGRESS', 'CANCELLED', 'LAUNCHING', 'QUEUED']),
      startTime: PropTypes.number,
      endTime: PropTypes.number
    }),
    gitInfo: PropTypes.obj
  })
};

export default BuildDetail;
