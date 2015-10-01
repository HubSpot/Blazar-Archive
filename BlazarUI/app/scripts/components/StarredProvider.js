import Cookies from 'js-cookie';

let starCache;

const StarredProvider = {

  starChange: function(isStarred, repo, branch) {

    if (isStarred) {
      this.removeStar(repo, branch);
    }
    else {
      this.addStar(repo, branch);
    }
  },

  addStar: function(repo, branch) {
    const starredRepos = this.getStars();
    if (this.hasStar({ repo: repo, branch: branch }) === -1) {
      starredRepos.push({ repo: repo, branch: branch });
    }
    Cookies.set('starred-repos', starredRepos, { expires: 3650 });
    starCache = starredRepos;
  },

  removeStar: function(repo, branch) {
    const starredRepos = this.getStars();
    const index = this.hasStar({ repo: repo, branch: branch });
    if (index !== -1) {
      starredRepos.splice(index, 1);
    }
    Cookies.set('starred-repos', starredRepos, { expires: 3650 });
    starCache = starredRepos;
  },

  getStars: function() {
    if (starCache === undefined) {
      this.syncStarCache();
    }
    return starCache;
  },

  hasStar: function(o) {
    const stars = this.getStars();
    for (let i = 0; i < stars.length; i++) {
        if (stars[i].repo === o.repo && stars[i].branch === o.branch) {
            return i;
        }
    }
    return -1;
  },

  syncStarCache: function() {
    const starredRepos = Cookies.getJSON('starred-repos');
    if (!starredRepos) {
      starredRepos = [];
    }
    starCache = starredRepos;
  }

};

export default StarredProvider;
