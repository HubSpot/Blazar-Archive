import React, {Component, PropTypes} from 'react';

class BuildLog extends Component {

  render() {

    return (
      <pre className='build-log'>
        {this.props.log}
      </pre>
    );
  }

}

BuildLog.propTypes = {
  log: PropTypes.string
};

export default BuildLog;
