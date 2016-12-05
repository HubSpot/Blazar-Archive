import React, { PropTypes } from 'react';
import humanizeDuration from 'humanize-duration';

const buildDurationHumanizer = humanizeDuration.humanizer({
  delimiter: ' ',
  units: ['m', 's'],
  round: true
});

const shortEnglishHumanizer = humanizeDuration.humanizer({
  language: 'shortEn',
  languages: {
    shortEn: {
      m: () => 'm',
      s: () => 's',
    }
  },
  delimiter: ' ',
  spacer: '',
  units: ['m', 's'],
  round: true
});

const BuildDuration = ({startTimestamp, endTimestamp, abbreviateUnits}) => {
  const duration = endTimestamp - startTimestamp;
  const formattedDuration = abbreviateUnits ? shortEnglishHumanizer(duration) : buildDurationHumanizer(duration);
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
