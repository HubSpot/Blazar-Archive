import Search from './search';
import {getStarredModules} from './starHelpers';
import {filter, has, sortBy} from 'underscore';

export const getFilterMatches = function(builds, filterText) {
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
