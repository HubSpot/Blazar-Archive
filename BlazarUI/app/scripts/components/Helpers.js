// Using in favor of Deprecated ComponentHelpers.js
import React from 'react';
import {some, uniq, flatten, filter, contains} from 'underscore';
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
    }
  });
}

export const uniqueModules = function(modules) {
  const uniqueModules = uniq(modules, false, (m) => {
    return m.module.name;
  });

  return uniqueModules.map((m) => {
    return {
      value: m.module.name,
      label: m.module.name
    }
  });
}

export const tableRowBuildState = function(state) {
  if (state === BuildStates.FAILED) {
    return 'bgc-danger';
  }
  else if (state === BuildStates.CANCELLED) {
    return 'bgc-warning';
  }
};

export const getFilteredModules = function(filters, modules) {
  const branchFilters = filters.branch;
  const moduleFilters = filters.module;

  const filteredModules = modules.filter((m) => {
    let passGo = false;

    // not filtering
    if (branchFilters.length === 0 && moduleFilters.length === 0) {
      return true;
    }

    // filtering both branch and module
    if (branchFilters.length > 0 && moduleFilters.length > 0) {
      let branchMatch = false;
      let moduleMatch = false;

      branchFilters.some((branch) => {
        if (branch.value === m.gitInfo.branch) {
          branchMatch = true;
        }
      });

      moduleFilters.some((module) => {
        if (module.value === m.module.name) {
          moduleMatch = true;
        }
      });
      
      return branchMatch && moduleMatch

    }

    if (branchFilters.length > 0) {        
      branchFilters.forEach((bf) => {
        if (m.gitInfo.branch === bf.value) {
          passGo = true;
        }
      });
    }

    if (moduleFilters.length > 0) {        
      moduleFilters.forEach((mf) => {
        if (m.module.name === mf.value) {
          passGo = true;
        }
      });
    }

    return passGo;
  });

  //finallay sort by branch and module name
  return filteredModules.sort( (a, b) => {
    return cmp(a.gitInfo.branch, b.gitInfo.branch) || cmp(a.module.name, b.module.name);
  });  
}

export const buildIsOnDeck = function(buildState) {
  return contains([BuildStates.LAUNCHING, buildState.QUEUED], buildState);
}

export const buildIsInactive = function(buildState) {
  contains([BuildStates.SUCCESS, BuildStates.FAILED, BuildStates.CANCELLED], buildState)
}

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
