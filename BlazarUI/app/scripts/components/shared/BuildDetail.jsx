import React, {Component, PropTypes} from 'react';
import {has, contains} from 'underscore';
import {humanizeText, timestampFormatted, truncate} from '../Helpers';
import classNames from 'classnames';

import Sha from '../shared/Sha.jsx';
import Alert from 'react-bootstrap/lib/Alert';

import BuildStates from '../../constants/BuildStates';
import FINAL_BUILD_STATES from '../../constants/finalBuildStates';
import {LABELS} from '../constants';

class BuildDetail extends Component {

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
        <small>in {build.duration}</small>
      );
    }

    if (build.state === BuildStates.IN_PROGRESS) {
      buildDetail.duration = (
        <small>started {timestampFormatted(build.startTimestamp)}</small>
      );
    }

    const sha = build.sha;
    const shaLink = `https://${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/commit/${sha}`;

    return (
      <div className={this.getWrapperClassNames()}>
        <div className='build-detail__topline'>
          <h4 className='build-detail__build-state'>
            Build {buildDetail.buildResult} {buildDetail.duration}
          </h4>        
        </div>  

        <div className='build-detail__commitInfo'>
          <p className='build-detail__sha'> commit <Sha gitInfo={gitInfo} build={build} /> </p>
          <p> Authored by {currentCommit.author.name} </p>
          <p> 
            {timestampFormatted(currentCommit.timestamp, 'dddd')}, { ' ' }
            {timestampFormatted(currentCommit.timestamp, 'lll')} 
          </p>
          <div className='build-detail__commit-desc'>
            <pre>{currentCommit.message}</pre>
          </div>
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
