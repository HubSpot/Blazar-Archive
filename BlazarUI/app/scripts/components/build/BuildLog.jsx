import React from 'react';

class BuildLog extends React.Component {

  render() {

    return (
      <pre className='build-log'>
        {this.props.log}
      </pre>
    );
  }

}

BuildLog.propTypes = {
  log: React.PropTypes.string
};

export default BuildLog;
