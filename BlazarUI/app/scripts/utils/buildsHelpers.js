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

export const binarySearch = (haystack, needle) => {
  return bs(haystack, needle, (a, b) => {
    return a.module.id - b.module.id;
  });
}

// to do - make this more reusable by property type
export const sortBuilds = (builds, type) => {
  switch (type) {
    case 'building':
      return sortBy(builds, function(b) {
        return -b.inProgressBuild.startTimestamp;
      });
    break;
    
    // change to module name..
    case 'abc':
      return sortBy(builds, function(b) {
        return b.module.name;
      });
    break;
    
    case 'repo':
      return sortBy(builds, function(b) {
        return b.repo;
      });
    break;

    default:
      return builds;
  }

};
