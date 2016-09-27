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
    return <BuildDuration startTimestamp={this.props.startTimestamp} endTimestamp={+moment()} />;
  }
}

BuildDurationStopWatch.propTypes = {
  startTimestamp: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.number,
    PropTypes.instanceOf(Date)
  ])
};

export default BuildDurationStopWatch;
