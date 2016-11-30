import React, { PropTypes } from 'react';
import classNames from 'classnames';
import {iconStatus} from '../constants';
import Icon from './Icon.jsx';

const BuildStateIcon = ({buildState, className}) => {
  const classes = classNames([
    'fa-stack',
    'build-state-icon',
    `build-state-icon--${buildState}`,
    className
  ]);

  const iconName = iconStatus[buildState];

  // do not wrap in a circle
  if (iconName === 'clock-o') {
    return (
      <span className={classes}>
        <Icon classNames="fa-stack-2x" name="clock-o" />
      </span>
    );
  }


  return (
    <span className={classes}>
      <Icon classNames="fa-stack-2x" name="circle" />
      <Icon classNames="fa-stack-1x build-state-icon--inner" name={iconName} />
    </span>
  );
};

BuildStateIcon.propTypes = {
  buildState: PropTypes.oneOf(Object.keys(iconStatus)).isRequired,
  className: PropTypes.string
};

export default BuildStateIcon;
