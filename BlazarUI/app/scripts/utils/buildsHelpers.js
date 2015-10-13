import Search from './search';
import {getStarredModules} from './starHelpers';
import {filter, has, sortBy} from 'underscore';
import bs from 'binary-search';

export const getFilterMatches = (builds, filterText) => {
  if (builds.length === 0) {
    return [];
  }

  const modulesSearch = new Search({ records: builds });
  return modulesSearch.match(filterText);
};

export const filterByToggle = (filterState, modules, stars) => {
  if (filterState === 'starred') {
    const starredModules = getStarredModules(stars, modules);
    return starredModules;
  }

  if (filterState === 'building') {
    const buildingModules = filter(modules, (module) => {
      return has(module, 'inProgressBuild');
    });

    modules = sortBy(buildingModules, function(r) {
      return -r.inProgressBuild.startTimestamp;
    });
  }

  return modules;
};

function binarySearch(haystack, needle) {
  return bs(haystack, needle, (a, b) => {
    return a.module.id - b.module.id;
  });
}

export const updateBuilds = (latestBuilds, currentBuilds) => {

  currentBuilds.sort((a, b) => {
    return a.module.id - b.module.id;
  });

  for (let i = 0, len = latestBuilds.length; i < len; i++) {
    const buildIndex = binarySearch(currentBuilds, latestBuilds[i]);

    if (buildIndex > 0) {
      currentBuilds[buildIndex] = latestBuilds[i];
    }

    else {
      currentBuilds.push(latestBuilds[i]);
    }

  }

  return currentBuilds;
};


export const sortBuilds = (builds) => {
  return sortBy(builds, function(b) {
    return b.module.name;
  });
};
