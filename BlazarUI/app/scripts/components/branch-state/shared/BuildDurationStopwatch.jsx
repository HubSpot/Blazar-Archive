import React, { Component, PropTypes } from 'react';
import moment from 'moment';
import BuildDuration from './BuildDuration.jsx';

class BuildDurationStopWatch extends Component {
  componentDidMount() {
    this.interval = setInterval(() => this.forceUpdate(), 1000);
  }

  componentWillUnmount() {
    clearInterval(this.interval);
  }

  render() {
    const {startTimestamp, abbreviateUnits} = this.props;
    return (
      <BuildDuration
        startTimestamp={startTimestamp}
        endTimestamp={+moment()}
        abbreviateUnits={abbreviateUnits}
      />
    );
  }
}

BuildDurationStopWatch.propTypes = {
  startTimestamp: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.number,
    PropTypes.instanceOf(Date)
  ]),
  abbreviateUnits: PropTypes.bool
};

BuildDurationStopWatch.defaultProps = {
  abbreviateUnits: false
};

export default BuildDurationStopWatch;
