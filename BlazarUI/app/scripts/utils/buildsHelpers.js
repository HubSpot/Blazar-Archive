import Search from './search';
import {sortBy} from 'underscore';
import bs from 'binary-search';

export const getFilterMatches = (builds, filterText) => {
  if (builds.length === 0) {
    return [];
  }

  if (filterText.length === 0) {
    return builds;
  }

  const modulesSearch = new Search({ records: builds });
  return modulesSearch.match(filterText);
};

export const binarySearch = (haystack, needle) => {
  return bs(haystack, needle, (a, b) => {
    return a.module.id - b.module.id;
  });
};

// to do - make this more reusable by property type
export const sortBuilds = (builds, type) => {
  switch (type) {
    case 'building':
      return sortBy(builds, (b) => {
        return -b.inProgressBuild.startTimestamp;
      });
    // change to module name..
    case 'abc':
      return sortBy(builds, (b) => {
        return b.module.name;
      });
    case 'repo':
      return sortBy(builds, (b) => {
        return b.repo;
      });
    default:
      return builds;
  }
};

export const sidebarCombine = (builds) => {
  const sidebarMap = {};

  builds.forEach((build) => {
    const {repository, branch} = build.gitInfo;
    let repoEntry;

    if (repository in sidebarMap) {
      repoEntry = sidebarMap[repository];
    } else {
      repoEntry = {};
    }

    repoEntry[branch] = build;
    sidebarMap[repository] = repoEntry;
  });

  return sidebarMap;
};
