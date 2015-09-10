/*global config*/
import _ from 'underscore';
import BaseCollection from './BaseCollection';

class Builds extends BaseCollection {

  url() {
    return `${config.apiRoot}/build/states`;
  }

  hasBuildState() {
    return _.filter(this.data, (item) => {
      return _.has(item, 'lastBuild') || _.has(item, 'inProgressBuild') || _.has(item, 'pendingBuild');
    });
  }

  isBuilding() {
    return _.filter(this.data, (item) => {
      return _.has(item, 'inProgressBuild');
    });
  }

  getReposByOrg(orgInfo) {
    const builds = this.hasBuildState();
    const orgBuilds = _.filter(builds, function(a) {
      return a.gitInfo.organization === orgInfo.org;
    });

    const repos = _.uniq(_.map(orgBuilds, function(build) {
      const latestBuild = (build.inProgressBuild ? build.inProgressBuild : build.lastBuild ? build.lastBuild : build.pendingBuild);
      latestBuild.module = build.module.name;
      latestBuild.branch = build.gitInfo.branch;
      const repo = {
        repo: build.gitInfo.repository,
        latestBuild: latestBuild
      };
      return repo;
    }), false, function(r) {
      return r.repo;
    });
    return _.sortBy(repos, function(r) {
      return r.repo.toLowerCase();
    });
  }

  getBranchModules(branchInfo) {
    const modules = _.findWhere(this.groupBuildsByRepo(), {
      organization: branchInfo.org,
      repository: branchInfo.repo,
      branch: branchInfo.branch
    });

    return modules || {};
  }

  getBranchesByRepo(repoInfo) {

    let branches = _.filter(this.groupBuildsByRepo(), (repo) => {
      const match = (repo.organization === repoInfo.org) && (repo.repository === repoInfo.repo);
      return match;
    });

    branches.sort( (a, b) => {
      return b.branch - a.branch;
    });

    // move master to top of branches list
    const masterIndex = branches.map(function(el) {
      return el.branch;
    }).indexOf('master');

    if (masterIndex > 0) {
      const master = branches.splice(masterIndex, masterIndex + 1);
      branches = master.concat(branches);
    }

    return branches;

  }


  groupBuildsByRepo() {
    // group and generate key, by org::repo[branch]
    const grouped = _.groupBy(this.data, function(o) {
      return `${o.gitInfo.organization}::${o.gitInfo.repository}[${o.gitInfo.branch}]`;
    });


    // move groupedBy object into an easier-to-work-with array
    let groupedInArray = [];
    for (let repo in grouped) {
      groupedInArray.push({
        repoModuleKey: repo,
        repository: grouped[repo][0].gitInfo.repository,
        isBuilding: grouped[repo].moduleIsBuilding,
        modules: grouped[repo]
      });
    }

    // store some helper properites
    groupedInArray.forEach( (repo) => {
      if (repo.modules[0].inProgressBuild) {
        repo.mostRecentBuild = repo.modules[0].inProgressBuild.startTimestamp;
      }
      repo.host = repo.modules[0].gitInfo.host;
      repo.branch = repo.modules[0].gitInfo.branch;
      repo.organization = repo.modules[0].gitInfo.organization;
      repo.id = `${repo.host}_${repo.branch}_${repo.organization}_${repo.repository}`;
      repo.branchPath = `${config.appRoot}/builds/${repo.modules[0].gitInfo.host}/${repo.modules[0].gitInfo.organization}/${repo.modules[0].gitInfo.repository}/${repo.modules[0].gitInfo.branch}`;


      let timesBuiltOnBlazar = 0;
      repo.hasBuiltOnBlazar = false;

      repo.modules.forEach( (module) => {
        module.modulePath = `${config.appRoot}/builds/${module.gitInfo.host}/${module.gitInfo.organization}/${module.gitInfo.repository}/${module.gitInfo.branch}/${module.module.name}`;

        if (_.has(module, 'lastBuild')) {
          timesBuiltOnBlazar++;
        }

        if (module.inProgressBuild) {
          module.inProgressBuild.buildLink = `${config.appRoot}/builds/${module.gitInfo.host}/${module.gitInfo.organization}/${module.gitInfo.repository}/${module.gitInfo.branch}/${module.module.name}/${module.inProgressBuild.buildNumber}`;
          if (module.inProgressBuild.startTimestamp < repo.mostRecentBuild) {
            repo.mostRecentBuild = module.inProgressBuild.startTimestamp;
          }
        }
      });

      if (timesBuiltOnBlazar > 0) {
        repo.hasBuiltOnBlazar = true;
      }

    });

    // sort by if building then by most recent build time
    function cmp(x, y) {
      return x > y ? 1 : x < y ? -1 : 0;
    }

    groupedInArray.sort( (a, b) => {
      return cmp(b.isBuilding, a.isBuilding) || cmp(a.mostRecentBuild, b.mostRecentBuild);
    });

    return groupedInArray;
  }






}

export default Builds;
