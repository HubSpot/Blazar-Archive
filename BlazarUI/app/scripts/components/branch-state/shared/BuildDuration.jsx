import React, { PropTypes } from 'react';
import humanizeDuration from 'humanize-duration';

const buildDurationHumanizer = humanizeDuration.humanizer({
  delimiter: ' ',
  units: ['m', 's'],
  round: true
});

const formatShort = (duration) => {
  const seconds = Math.floor(duration / 1000);
  const minutes = Math.floor(seconds / 60);
  const formattedSeconds = `${seconds % 60}s`;
  return minutes ? `${minutes}m ${formattedSeconds}` : formattedSeconds;
};

const BuildDuration = ({startTimestamp, endTimestamp, abbreviateUnits}) => {
  const duration = endTimestamp - startTimestamp;
  const formattedDuration = abbreviateUnits ? formatShort(duration) : buildDurationHumanizer(duration);
  return <strong>{formattedDuration}</strong>;
};

BuildDuration.propTypes = {
  startTimestamp: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.number
  ]),
  endTimestamp: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.number
  ]),
  abbreviateUnits: PropTypes.bool
};

BuildDuration.defaultProps = {
  abbreviateUnits: false
};

export default BuildDuration;
