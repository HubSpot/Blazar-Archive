import React, {Component, PropTypes} from 'react';
import {has, contains, bindAll} from 'underscore';
import {humanizeText, timestampFormatted, truncate} from '../Helpers';
import classNames from 'classnames';

import Loader from '../shared/Loader.jsx';
import RepoBuildCancelButton from './RepoBuildCancelButton.jsx';
import Sha from '../shared/Sha.jsx';
import Alert from 'react-bootstrap/lib/Alert';

import BuildStates from '../../constants/BuildStates';
import FINAL_BUILD_STATES from '../../constants/finalBuildStates';
import {LABELS} from '../constants';

class RepoBuildBuildDetail extends Component {

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
  
  render() {
    const {currentRepoBuild} = this.props

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
    
    const build = this.props.currentRepoBuild.toJS();
    const currentCommit = build.commitInfo.current
    const newCommits = build.commitInfo.newCommits

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
    
    if (!has(build, 'commitInfo')) {
      return (
        <Alert>No detail available for this build</Alert>
      );
    }
  
    if (contains(FINAL_BUILD_STATES, build.state)) {
      buildDetail.endtime = timestampFormatted(build.endTimestamp)
      const conjunction = build.state === BuildStates.CANCELLED ? 'after' : 'in';

      buildDetail.duration = (
        <span>{conjunction} {build.duration}</span>
      );
    } 

    else if (build.state === BuildStates.IN_PROGRESS) {
      buildDetail.duration = (
        <span>started {timestampFormatted(build.startTimestamp)}</span>
      );
    }

    return (
      <div className={this.getWrapperClassNames(build)}>
        <div className='build-detail-header'>
          <p className='build-detail-header__build-state'>
            Build {buildDetail.buildResult} 
            <span className='build-detail-header__timestamp'>{buildDetail.duration}</span>
          </p>
          <RepoBuildCancelButton 
            triggerCancelBuild={this.props.triggerCancelBuild}
            build={build}
          />
        </div>
        <div className='build-detail-body'>
          <pre className='build-detail-body__commit-desc' title={currentCommit.message}>{truncate(currentCommit.message, this.state.windowWidth * .08, true)}</pre>
        </div>
        <div className='build-detail-footer'>
          Triggered by <strong>{currentCommit.author.name}</strong> on { ' ' }
          {timestampFormatted(currentCommit.timestamp, 'llll')} 
          <span className='build-detail__sha'> 
            commit <Sha gitInfo={gitInfo} build={build} />
          </span>
        </div>
      </div>
    );
  }

}


RepoBuildBuildDetail.propTypes = {
  loading: PropTypes.bool.isRequired,
  currentRepoBuild: PropTypes.object,
  error: PropTypes.string
};

export default RepoBuildBuildDetail;
