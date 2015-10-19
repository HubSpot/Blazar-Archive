import React, {Component} from 'react';
import Search from './search';
import {sortBy} from 'underscore';
import bs from 'binary-search';

export const getFilterMatches = (builds, filterText) => {
  if (builds.length === 0) {
    return [];
  }

  const modulesSearch = new Search({ records: builds });
  return modulesSearch.match(filterText);
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

export const sortBuilds = (builds, type) => {
  switch (type) {
    case 'building':
      return sortBy(builds, function(b) {
        return -b.inProgressBuild.startTimestamp;
      });
    break;

    case 'abc':
      return sortBy(builds, function(b) {
        return b.module.name;
      });
    break;

    default:
      return builds;
  }

};
