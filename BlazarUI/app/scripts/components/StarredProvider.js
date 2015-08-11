import Cookies from 'js-cookie';

let StarredProvider = {

  addStar: function(repo, branch) {
    let starredRepos = this.getStars();
    if (this.hasStar({ repo: repo, branch: branch }) === -1) {
      starredRepos.push({ repo: repo, branch: branch });
    }
    Cookies.set('starred-repos', starredRepos);
  },

  removeStar: function(repo, branch) {
    let starredRepos = this.getStars();
    let index = this.hasStar({ repo: repo, branch: branch });
    if (index !== -1) {
      starredRepos.splice(index, 1);
    }
    Cookies.set('starred-repos', starredRepos);
  },

  getStars: function() {
    let starredRepos = Cookies.getJSON('starred-repos');
    if (!starredRepos) {
      starredRepos = [];
    }
    return starredRepos;
  },

  hasStar: function(o) {
    let stars = this.getStars();
    for (var i = 0; i < stars.length; i++) {
        if (stars[i].repo == o.repo && stars[i].branch == o.branch) {
            return i;
        }
    }
    return -1;
  }

}

export default StarredProvider;
