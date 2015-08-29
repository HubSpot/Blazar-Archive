import Cookies from 'js-cookie';

let watchingCache;

let WatchingProvider = {

  addWatch: function(repo, branch) {
    let watching = this.getWatching();
    if (this.isWatching({ repo: repo, branch: branch }) === -1) {
      watching.push({ repo: repo, branch: branch });
    }
    Cookies.set('watching-repos', watching, { expires: 3650 });
    watchingCache = watching;
  },

  removeWatch: function(repo, branch) {
    let watching = this.getWatching();
    let index = this.isWatching({ repo: repo, branch: branch });
    if (index !== -1) {
      watching.splice(index, 1);
    }
    Cookies.set('watching-repos', watching, { expires: 3650 });
    watchingCache = watching;
  },

  getWatching: function() {
    if (watchingCache === undefined) {
      this.syncWatchingCache();
    }
    return watchingCache;
  },

  isWatching: function(o) {
    let watching = this.getWatching();
    for (let i = 0; i < watching.length; i++) {
        if (watching[i].repo === o.repo && watching[i].branch === o.branch) {
            return i;
        }
    }
    return -1;
  },

  syncWatchingCache: function() {
    let watching = Cookies.getJSON('watching-repos');
    if (!watching) {
      watching = [];
    }
    watchingCache = watching;
  }

};

export default WatchingProvider;
