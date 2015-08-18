import React, {Component, PropTypes} from 'react';
import SectionLoader from '../shared/SectionLoader.jsx';
import AnsiUp from 'ansi_up';

class BuildLog extends Component {

  render() {
    if (this.props.loading) {
      return <SectionLoader />;
    }

    if (!this.props.log || typeof this.props.log !== 'string') {
      return <div></div>;
    }

    return (
      <pre className='build-log'
        dangerouslySetInnerHTML={{__html: AnsiUp.ansi_to_html(AnsiUp.escape_for_html(this.props.log))}}>
      </pre>
    );
  }

}

BuildLog.propTypes = {
  log: PropTypes.string,
  loading: PropTypes.bool
};

export default BuildLog;
