import React from 'react';

const buildLables = {
  'SUCCEEDED': 'success',
  'FAILED': 'danger',
  'IN_PROGRESS': 'default'
};

class LastBuild extends React.Component {

  getClassNames() {
    return 'build-detail alert alert-' + buildLables[this.props.build.result];
  }

  render() {
    let {commit, buildNumber} = this.props.build;
    return (
      <div className={this.getClassNames()}>
        <p> #{buildNumber} - 2 days, 22 hr ago. Ran for 7 min 34 sec. </p>
        <pre className='commit'>
          [mavin-release-plugin] prepare for the next development iteration
          <br/><a href="#">{commit}</a>
        </pre>
        <p>Started by Github push by ssalinas</p>
      </div>
    );
  }

}

LastBuild.propTypes = {
  build: React.PropTypes.shape({
    buildNumber: React.PropTypes.number,
    startTime: React.PropTypes.number,
    endTime: React.PropTypes.number,
    commit: React.PropTypes.string,
    result: React.PropTypes.oneOf(['SUCCEEDED', 'FAILED'])
  })
};

export default LastBuild;
