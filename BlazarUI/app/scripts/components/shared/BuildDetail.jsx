import React from 'react';
import Helpers from '../ComponentHelpers';

const buildLables = {
  'SUCCEEDED': 'success',
  'FAILED': 'danger',
  'IN_PROGRESS': 'info',
  'CANCELLED': 'warning'
};

class BuildDetail extends React.Component {

  getClassNames() {
    return 'build-detail alert alert-' + buildLables[this.props.build.buildState.result];
  }

  render() {

    if (this.props.loading) {
      return <div></div>;
    }

    let {buildState, gitInfo} = this.props.build;
    let endtime, duration;
    // to do: add build result icon
    let buildResult = Helpers.humanizeText(buildState.result);

    if (buildState.result !== 'IN_PROGRESS') {
      endtime = 'on ' + Helpers.timestampFormatted(buildState.endTime);
      duration = 'Ran for ' + buildState.duration;
    }

    let sha = buildState.commitSha;
    let shaLink = `https://${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/commit/${sha}`;

    return (
      <div className={this.getClassNames()}>
        <h4 className='build-detail__build-state'>
          {buildResult} <small>{endtime}</small>
        </h4>
        <p>{duration}</p>
        <a target="_blank" href={shaLink}>{buildState.commitSha}</a>
      </div>
    );
  }

}

BuildDetail.propTypes = {
  loading: React.PropTypes.bool.isRequired,
  build: React.PropTypes.shape({
    buildState: React.PropTypes.shape({
      buildNumber: React.PropTypes.number,
      commitSha: React.PropTypes.string,
      result: React.PropTypes.oneOf(['SUCCEEDED', 'FAILED', 'IN_PROGRESS', 'CANCELLED']),
      startTime: React.PropTypes.number,
      endTime: React.PropTypes.number
    }),
    gitInfo: React.PropTypes.obj
  })
};

export default BuildDetail;
