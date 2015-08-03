import React, {Component, PropTypes} from 'react';
import Helpers from '../ComponentHelpers';

const buildLables = {
  'SUCCEEDED': 'success',
  'FAILED': 'danger',
  'IN_PROGRESS': 'info',
  'CANCELLED': 'warning'
};

class BuildDetail extends Component {

  getClassNames() {
    return 'build-detail alert alert-' + buildLables[this.props.build.build.state];
  }

  render() {

    let {build, gitInfo} = this.props.build;
    let endtime, duration;
    let buildResult = Helpers.humanizeText(build.state);

    if (build.state !== 'IN_PROGRESS') {
      endtime = 'On ' + Helpers.timestampFormatted(build.endTime);
      duration = 'Ran for ' + build.duration;
    }

    let sha = build.sha;
    let shaLink = `https://${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/commit/${sha}`;
    let buildDetail;

    if (build.result === 'IN_PROGRESS') {
      buildDetail = 'started ' + Helpers.timestampFormatted(build.startTime);
    } else {
      buildDetail = endtime;
    }

    return (
      <div className={this.getClassNames()}>
        <h4 className='build-detail__build-state'>
          Build {buildResult} <small>{buildDetail}</small>
        </h4>
        <p>{duration}</p>
        <p>
          Commit: <a target="_blank" href={shaLink}>{build.sha}</a>
        </p>
      </div>
    );
  }

}

BuildDetail.propTypes = {
  loading: PropTypes.bool.isRequired,
  build: PropTypes.shape({
    buildState: PropTypes.shape({
      buildNumber: PropTypes.number,
      commitSha: PropTypes.string,
      result: PropTypes.oneOf(['SUCCEEDED', 'FAILED', 'IN_PROGRESS', 'CANCELLED']),
      startTime: PropTypes.number,
      endTime: PropTypes.number
    }),
    gitInfo: PropTypes.obj
  })
};

export default BuildDetail;
