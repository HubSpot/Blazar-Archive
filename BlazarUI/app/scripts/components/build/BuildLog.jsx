import React, {Component, PropTypes} from 'react';

class BuildLog extends Component {

  render() {
    if (this.props.loading || !this.log) {
      return  <div></div>;
    }

    return (
      <pre className='build-log'>
        {this.props.log}
      </pre>
    );
  }

}

BuildLog.propTypes = {
  log: PropTypes.string,
  loading: PropTypes.bool
};

export default BuildLog;
