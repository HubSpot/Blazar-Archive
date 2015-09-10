import React, {Component, PropTypes} from 'react';
import Collapsable from '../shared/Collapsable.jsx';
import SectionLoader from '../shared/SectionLoader.jsx';
import utf8 from 'utf8';

class BuildLog extends Component {

  render() {
    if (this.props.loading) {
      return <SectionLoader />;
    }

    if (!this.props.log || typeof this.props.log !== 'string') {
      return <div></div>;
    }

    return (
      <Collapsable 
        header='Build Log'
        initialToggleStateOpen={true}
        noBorder={true}
      >
        <pre className='build-log'>
          {utf8.decode(this.props.log)}
        </pre>
      </Collapsable>
    );
  }
}

BuildLog.propTypes = {
  log: PropTypes.string,
  loading: PropTypes.bool
};

export default BuildLog;
