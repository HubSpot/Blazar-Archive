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

    let {buildState} = this.props.build;
    // to do: add build result icon
    let buildResult = Helpers.humanizeText(buildState.result);

    return (
      <div className={this.getClassNames()}>
        <h4 className='build-detail__build-state'>
          {buildResult}
        </h4>
        <p> #{buildState.buildNumber} - 2 days, 22 hr ago. Ran for {buildState.duration} </p>
        <a href="#">{buildState.commitSha}</a>
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
    })
  })
};

export default BuildDetail;
