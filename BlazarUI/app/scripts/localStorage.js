import Immutable from 'immutable';
import store from 'store';

// branches, not repos, are starred, but the app
// current saves the ids as 'starredRepos'
const migrateStarredRepos = () => {
  const starredRepos = store.get('starredRepos');
  if (starredRepos) {
    store.set('starredBranches', starredRepos);
    store.remove('starredRepos');
  }
};

export const loadStarredBranches = () => {
  migrateStarredRepos();
  return Immutable.Set(store.get('starredBranches') || []);
};

export const saveStarredBranches = (starredBranches) => {
  store.set('starredBranches', starredBranches.toJS());
};
