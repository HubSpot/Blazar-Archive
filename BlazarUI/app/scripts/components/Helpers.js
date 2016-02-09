import React from 'react';
import {some, uniq, flatten, filter, contains} from 'underscore';
import humanizeDuration from 'humanize-duration';
import moment from 'moment';
import BuildStates from '../constants/BuildStates.js';
import {LABELS, iconStatus} from './constants';
import Icon from './shared/Icon.jsx';
import IconStack from './shared/IconStack.jsx';
import Immutable from 'Immutable';
import classNames from 'classNames';

// 1234567890 => 1 Aug 1991 15:00
export const timestampFormatted = function(timestamp, format='lll') {
  timestamp = parseInt(timestamp);
  if (!timestamp) {
    return '';
  }
  const timeObject = moment(timestamp);
  return timeObject.format(format);
};

export const timestampDuration = function(startTimestamp, endTimestamp, round='true') {
  return humanizeDuration(endTimestamp - startTimestamp, {round: round});
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

export const cmp = function(x, y) {
  return x > y ? 1 : x < y ? -1 : 0;
};

export const getIsStarredState = function(stars, id) {
  return some(stars, (star) => {
    return star.moduleId === id;
  });
};

// Data Helpers
export const uniqueBranches = function(branches) {
  const uniqueBranches = uniq(branches, false, (b) => {
    return b.gitInfo.branch;
  });

  return uniqueBranches.map((b) => {
    return {
      value: b.gitInfo.branch,
      label: b.gitInfo.branch
    };
  });
};

export const tableRowBuildState = function(state) {
  if (state === BuildStates.FAILED) {
    return 'bgc-danger';
  }
  else if (state === BuildStates.CANCELLED) {
    return 'bgc-warning';
  }
};

export const getFilteredBranches = function(filters, branches) {
  
  const branchFilters = filters.branch;

  const filteredBranches = branches.filter((b) => {
    let passGo = false;
  
    // not filtering
    if (branchFilters.length === 0) {
      return true;
    }
  
    if (branchFilters.length > 0) {
      let branchMatch = false;
  
      branchFilters.some((branch) => {
        if (branch.value === b.gitInfo.branch) {
          branchMatch = true;
        }
      });
      
      return branchMatch;
    }
  
    return passGo;
  });
  
  //finally sort by branch and bodule name
  return filteredBranches.sort((a, b) => {
    return cmp(a.gitInfo.branch, b.gitInfo.branch);
  });  
};

export const buildIsOnDeck = function(buildState) {
  return contains([BuildStates.LAUNCHING, BuildStates.QUEUED], buildState);
};

export const buildIsInactive = function(buildState) {
  return contains([BuildStates.SUCCESS, BuildStates.FAILED, BuildStates.CANCELLED], buildState);
};

// DOM Helpers
export const events = {
  listenTo: function(event, cb) {
    window.addEventListener(event, cb);
  },
  removeListener: function(event, cb) {
    window.removeEventListener(event, cb);
  }
};

export const dataTagValue = function(e, tagName) {
  const currentTarget = e.currentTarget;
  return currentTarget.getAttribute(`data-${tagName}`);
};

export const scrollTo = function(direction) {
  if (direction === 'bottom') {
    window.scrollTo(0, document.body.scrollHeight);
  }
  else if (direction === 'top') {
    window.scrollTo(0, 0);
  }
};

export const getPathname = function() {
  return window.location.pathname;
};

// To do: move these out as components in components/shared
export const buildResultIcon = function(result, prevBuildState='') {
  let classNames = LABELS[result];
  let resultSymbol = iconStatus[result];
  let iconNames = Immutable.List.of(iconStatus[result]);

  if (result == BuildStates.IN_PROGRESS) {
    classNames = getBuildStatusIconClassNames(result, prevBuildState);
    resultSymbol = iconStatus[prevBuildState];
    iconNames = Immutable.List.of(iconStatus[prevBuildState]);
  }

  return (
    <div className="table-icon-container">
      <IconStack 
        iconStackBase="circle"
        iconNames={iconNames}
        classNames={classNames}
      />
    </div>
  );
};

export const getBuildStatusIconClassNames = function(result, prevBuildState) {
  let prevBuildStateModifier = ``;

  if (result === BuildStates.IN_PROGRESS && prevBuildState) {
    prevBuildStateModifier = `-laststatus-${prevBuildState}`;
  }

  return classNames([
    'building-icon',
    `building-icon--${result}${prevBuildStateModifier}`
  ]);
};
