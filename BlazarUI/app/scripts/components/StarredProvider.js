import Cookies from 'js-cookie';

let StarredProvider = {

  addStar: function(repo) {
    let starredRepos = this.getStars();
    if (starredRepos.indexOf(repo) === -1) {
      starredRepos.push(repo);
    }
    Cookies.set('starred-repos', starredRepos);
  },

  removeStar: function(repo) {
    let starredRepos = this.getStars();
    let index = starredRepos.indexOf(repo);
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
  }

}

export default StarredProvider;
