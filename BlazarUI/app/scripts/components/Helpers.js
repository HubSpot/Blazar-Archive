import React from 'react';
import {uniq, contains} from 'underscore';
import humanizeDuration from 'humanize-duration';
import moment from 'moment';
import BuildStates from '../constants/BuildStates.js';
import FINAL_BUILD_STATES from '../constants/finalBuildStates';
import ACTIVE_BUILD_STATES from '../constants/ActiveBuildStates';
import QUEUED_BUILD_STATES from '../constants/QueuedBuildStates';
import {iconStatus} from './constants';
import IconStack from './shared/IconStack.jsx';
import Immutable from 'immutable';
import classNames from 'classnames';
import Cookies from 'js-cookie';

export const getUsernameFromCookie = () => {
  if (!window.config.usernameCookie) {
    return undefined;
  }

  return Cookies.get(window.config.usernameCookie);
};

// 1234567890 => 1 Aug 1991 15:00
export const timestampFormatted = (timestamp, format = 'lll') => {
  timestamp = parseInt(timestamp, 10);
  if (!timestamp) {
    return '';
  }
  const timeObject = moment(timestamp);
  return timeObject.format(format);
};

export const timestampDuration = (startTimestamp, endTimestamp, round = 'true') => {
  return humanizeDuration(endTimestamp - startTimestamp, {round});
};

// 'BUILD_SUCCEEEDED' => 'Build Succeeded'
export const humanizeText = (string) => {
  if (!string) {
    return '';
  }
  string = string.replace(/_/g, ' ');
  string = string.toLowerCase();
  string = string[0].toUpperCase() + string.substr(1);
  return string;
};

export const truncate = (str, len = 10, ellip = false) => {
  if (str && str.length > len && str.length > 0) {
    let newStr = `${str} `;
    newStr = str.substr(0, len);
    newStr = str.substr(0, newStr.lastIndexOf(' '));
    newStr = (newStr.length > 0) ? newStr : str.substr(0, len);
    if (ellip && str.length > len) {
      newStr += '…';
    }
    return newStr;
  }
  return str;
};

export const githubShaLink = (info) => {
  return `https://${info.gitInfo.host}/${info.gitInfo.organization}/${info.gitInfo.repository}/commit/${info.build.sha}/`;
};

export const cmp = (x, y) => {
  if (x > y) {
    return 1;
  } else if (x < y) {
    return -1;
  }

  return 0;
};

// Data Helpers
export const getUniqueBranches = (branches) => {
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

export const tableRowBuildState = (state) => {
  if (state === BuildStates.FAILED) {
    return 'bgc-danger';
  } else if (state === BuildStates.CANCELLED || state === BuildStates.UNSTABLE) {
    return 'bgc-warning';
  }

  return null;
};

export const getFilteredBranches = (filters, branches) => {
  const branchFilters = filters.branch;

  const filteredBranches = branches.filter((b) => {
    const passGo = false;

    // not filtering
    if (branchFilters.length === 0) {
      return true;
    }

    if (branchFilters.length > 0) {
      let branchMatch = false;

      branchFilters.forEach((branch) => {
        if (branch.value === b.gitInfo.branch) {
          branchMatch = true;
        }
      });

      return branchMatch;
    }

    return passGo;
  });

  // finally sort by branch and bodule name
  return filteredBranches.sort((a, b) => {
    return cmp(a.gitInfo.branch, b.gitInfo.branch);
  });
};

export const buildIsOnDeck = (buildState) => {
  return contains(QUEUED_BUILD_STATES, buildState);
};

export const buildIsInactive = (buildState) => {
  return contains(FINAL_BUILD_STATES, buildState);
};

// DOM Helpers
export const events = {
  listenTo(event, cb) {
    window.addEventListener(event, cb);
  },
  removeListener(event, cb) {
    window.removeEventListener(event, cb);
  }
};

export const dataTagValue = (e, tagName) => {
  const currentTarget = e.currentTarget;
  return currentTarget.getAttribute(`data-${tagName}`);
};

export const scrollTo = (direction) => {
  if (direction === 'bottom') {
    window.scrollTo(0, document.body.scrollHeight);
  } else if (direction === 'top') {
    window.scrollTo(0, 0);
  }
};

export const getPathname = () => {
  return window.location.pathname;
};

export const getBuildStatusIconClassNames = (result) => {
  return classNames([
    'building-icon',
    `building-icon--${result}`
  ]);
};

// To do: move these out as components in components/shared
export const buildResultIcon = (result) => {
  const iconClassNames = getBuildStatusIconClassNames(result);
  const iconNames = Immutable.List.of(iconStatus[result]);

  return (
    <div className="table-icon-container">
      <IconStack
        iconStackBase="circle"
        iconNames={iconNames}
        classNames={iconClassNames}
      />
    </div>
  );
};

export const getPreviousBuildState = (builds) => {
  const numBuilds = builds.size;

  if (numBuilds <= 1) {
    return '';
  }

  const completedBuilds = builds.filter((build) => {
    return contains(FINAL_BUILD_STATES, build.get('state'));
  });

  if (completedBuilds.size === 0) {
    return '';
  }

  return completedBuilds.get(0).get('state');
};

export const sortBuildsByRepoAndBranch = (builds) => {
  return builds.sort((a, b) => {
    // Sort by repo, DESC
    const repoNameA = a.gitInfo.repository.toLowerCase();
    const repoNameB = b.gitInfo.repository.toLowerCase();

    if (repoNameA < repoNameB) {
      return -1;
    } else if (repoNameA > repoNameB) {
      return 1;
    }

    // Sort by branch, master at top
    const branchNameA = a.gitInfo.branch.toLowerCase();
    const branchNameB = b.gitInfo.branch.toLowerCase();

    const branchAIsMaster = branchNameA === 'master';
    const branchBIsMaster = branchNameB === 'master';

    if (branchAIsMaster || branchBIsMaster) {
      return (branchBIsMaster ? 1 : 0) - (branchAIsMaster ? 1 : 0);
    }

    // Sort by branch, DESC
    return branchNameA.localeCompare(branchNameB);
  });
};

export const sortBuildsByRepoAndBranchImmutable = (builds) => {
  return Immutable.fromJS(sortBuildsByRepoAndBranch(builds.toJS()));
};

export const filterInactiveBuilds = (builds) => {
  return builds.filter((build) => {
    return build.gitInfo.active;
  });
};

export const filterInactiveBuildsImmutable = (builds) => {
  return Immutable.fromJS(filterInactiveBuilds(builds.toJS()));
};

export const getTableDurationText = (state, duration) => {
  if ((state !== BuildStates.IN_PROGRESS && contains(ACTIVE_BUILD_STATES, state)) || state === BuildStates.SKIPPED) {
    return humanizeText(state);
  }

  return duration;
};

export const sortBranchesByTimestamp = (builds, isMasterPinned = true) => {
  return builds.sort((a, b) => {
    // master at top if master is pinned

    if (isMasterPinned) {
      if (a.gitInfo.branch === 'master') {
        return 1;
      } else if (b.gitInfo.branch === 'master') {
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
    } else if (buildB === undefined) {
      if (buildA === undefined) {
        return 0;
      }

      return -1;
    }

    return buildB.startTimestamp - buildA.startTimestamp;
  });
};
