// Using in favor of Deprecated ComponentHelpers.js
import React from 'react';
import {some} from 'underscore';
import moment from 'moment';
import BuildStates from '../constants/BuildStates.js';
import {LABELS, iconStatus} from './constants';
import Icon from './shared/Icon.jsx';

// 1234567890 => 1 Aug 1991 15:00
export const timestampFormatted = function(timestamp, format='lll') {
  timestamp = parseInt(timestamp);
  if (!timestamp) {
    return '';
  }
  const timeObject = moment(timestamp);
  return timeObject.format(format);
};

// 'BUILD_SUCCEEEDED' => 'Build Succeeded'
export const humanizeText = function(string) {
  if (!string) {
    return '';
  }
  string = string.replace(/_/g, ' ');
  string = string.toLowerCase();
  string = string[0].toUpperCase() + string.substr(1);
  return string;
};

export const truncate = function(str, len = 10, ellip=false) {
  if (str && str.length > len && str.length > 0) {
    let new_str = str + ' ';
    new_str = str.substr(0, len);
    new_str = str.substr(0, new_str.lastIndexOf(' '));
    new_str = (new_str.length > 0) ? new_str : str.substr(0, len);
    if (ellip && str.length > len) {
      new_str += '…';
    }
    return new_str;
  }
  return str;
};

export const githubShaLink = function(info) {
  return `https://${info.gitInfo.host}/${info.gitInfo.organization}/${info.gitInfo.repository}/commit/${info.build.sha}/`;
};

export const events = {
  listenTo: function(event, cb) {
    window.addEventListener(event, cb);
  },
  removeListener: function(event, cb) {
    window.removeEventListener(event, cb);
  }
};

export const getIsStarredState = function(stars, id) {
  return some(stars, (star) => {
    return star.moduleId === id;
  });
};

export const getPathname = function() {
  return window.location.pathname;
};

export const scrollTo = function(direction) {
  if (direction === 'bottom') {
    window.scrollTo(0, document.body.scrollHeight);
  }
  else if (direction === 'top') {
    window.scrollTo(0, 0);
  }
};

export const tableRowBuildState = function(state) {
  if (state === BuildStates.FAILED) {
    return 'bgc-danger';
  }
  else if (state === BuildStates.CANCELLED) {
    return 'bgc-warning';
  }
};


// Components
export const buildResultIcon = function(result) {
  const classNames = LABELS[result];

  return (
    <Icon
      name={iconStatus[result]}
      classNames={classNames}
      title={humanizeText(result)}
    />
  );
};
