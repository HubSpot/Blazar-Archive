/*global config*/
import React from 'react';
import {some, uniq, flatten, filter, contains} from 'underscore';
import humanizeDuration from 'humanize-duration';
import moment from 'moment';
import BuildStates from '../constants/BuildStates.js';
import FINAL_BUILD_STATES from '../constants/finalBuildStates';
import ACTIVE_BUILD_STATES from '../constants/ActiveBuildStates';
import QUEUED_BUILD_STATES from '../constants/QueuedBuildStates';
import {LABELS, iconStatus} from './constants';
import Icon from './shared/Icon.jsx';
import IconStack from './shared/IconStack.jsx';
import Immutable from 'immutable';
import classNames from 'classnames';
import Cookies from 'js-cookie';

export const getUsernameFromCookie = function() {
  if (!config.usernameCookie) {
    return undefined;
  }

  return Cookies.get(config.usernameCookie);
};

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
  if (state === BuildStates.FAILED || state === BuildStates.UNSTABLE) {
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
  return contains(QUEUED_BUILD_STATES, buildState);
};

export const buildIsInactive = function(buildState) {
  return contains(FINAL_BUILD_STATES, buildState);
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
  const classNames = getBuildStatusIconClassNames(result);
  const iconNames = Immutable.List.of(iconStatus[result]);

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

export const getBuildStatusIconClassNames = function(result) {
  return classNames([
    'building-icon',
    `building-icon--${result}`
  ]);
};

export const getPreviousBuildState = function(builds) {
  const numBuilds = builds.size;

  if (numBuilds <= 1) {
    return '';
  }

  let completedBuilds = builds.filter(function(build, i) {
    return contains(FINAL_BUILD_STATES, build.get('state'));
  });

  if (completedBuilds.size === 0) {
    return '';
  }

  return completedBuilds.get(0).get('state');
};

export const sortBuildsByRepoAndBranchImmutable = function(builds) {
  return Immutable.fromJS(sortBuildsByRepoAndBranch(builds.toJS()));
};

export const sortBuildsByRepoAndBranch = function(builds) {
  return builds.sort((a, b) => {

    // Sort by repo, DESC
    let repoNameA = a.gitInfo.repository.toLowerCase();
    let repoNameB = b.gitInfo.repository.toLowerCase();

    if (repoNameA < repoNameB) {
      return -1;
    }

    else if (repoNameA > repoNameB) {
      return 1;
    }

    // Sort by branch, master at top
    let branchNameA = a.gitInfo.branch.toLowerCase();
    let branchNameB = b.gitInfo.branch.toLowerCase();

    let branchAIsMaster = branchNameA === 'master';
    let branchBIsMaster = branchNameB === 'master';

    if (branchAIsMaster || branchBIsMaster) {
      return (branchBIsMaster ? 1 : 0) - (branchAIsMaster ? 1 : 0);
    }

    // Sort by branch, DESC
    return branchNameA.localeCompare(branchNameB);
  });
};

export const filterInactiveBuildsImmutable = function(builds) {
  return Immutable.fromJS(filterInactiveBuilds(builds.toJS()));
};

export const filterInactiveBuilds = function(builds) {
  return builds.filter((build) => {
    return build.gitInfo.active;
  });
};

export const getTableDurationText = function(state, duration) {
  if ((state !== BuildStates.IN_PROGRESS && contains(ACTIVE_BUILD_STATES, state)) || state === BuildStates.SKIPPED) {
    return humanizeText(state);
  }

  return duration;
};

export const sortBranchesByTimestamp = function(builds, isMasterPinned = true) {
  return builds.sort((a, b) => {
    // master at top if master is pinned

    if (isMasterPinned) {
      if (a.gitInfo.branch === 'master') {
        return 1;
      }

      else if (b.gitInfo.branch === 'master') {
        return -1;
      }
    }

    // prefer in progress builds' timestamps
    const buildA = a.inProgressBuild !== undefined ? a.inProgressBuild : a.lastBuild;
    const buildB = b.inProgressBuild !== undefined ? b.inProgressBuild : b.lastBuild;

    if (buildA === undefined) {
      if (buildB === undefined) {
        return 0;
      }

      return 1;
    }

    else if (buildB === undefined) {
      if (buildA === undefined) {
        return 0;
      }

      return -1;
    }

    return buildB.startTimestamp - buildA.startTimestamp;
  });
};
