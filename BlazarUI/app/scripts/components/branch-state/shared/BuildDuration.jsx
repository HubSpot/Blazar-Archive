import React, { PropTypes } from 'react';
import humanizeDuration from 'humanize-duration';

const buildDurationHumanizer = humanizeDuration.humanizer({
  delimiter: ' ',
  units: ['m', 's'],
  round: true
});

const BuildDuration = ({startTimestamp, endTimestamp}) => {
  const duration = endTimestamp - startTimestamp;
  return <strong>{buildDurationHumanizer(duration)}</strong>;
};

BuildDuration.propTypes = {
  startTimestamp: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.number
  ]),
  endTimestamp: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.number
  ])
};

export default BuildDuration;
