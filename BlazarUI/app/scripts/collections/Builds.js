/*global config*/
import _ from 'underscore';
import BaseCollection from './BaseCollection';

class Builds extends BaseCollection {

  url() {
    return `${config.apiRoot}/build/states`;
  }

  parse() {
    this.addTimeHelpers();
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


  getAllBuilds() {

    return _.map(this.data, (item) => {

      let {
        gitInfo,
        module,
        lastBuild,
        inProgressBuild
      } = item;

      if (_.has(item, 'inProgressBuild')) {
        item.inProgressBuild.blazarPath = `${config.appRoot}/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${gitInfo.branch}/${module.name}/${inProgressBuild.buildNumber}`;
      }

      if (_.has(item, 'lastBuild')) {
        item.lastBuild.blazarPath = `${config.appRoot}/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${gitInfo.branch}/${module.name}/${lastBuild.buildNumber}`;
      }

      if (_.has(item, 'module', (item) )) {
        item.module.blazarPath = `${config.appRoot}/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${gitInfo.branch}/${module.name}`;
      }

      gitInfo.branchBlazarPath = `${config.appRoot}/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${gitInfo.branch}`;

      return item;
    });


  }

  getReposByOrg(orgInfo) {
    let builds = this.hasBuildState();
    let orgBuilds = _.filter(builds, function(a) {
      return a.gitInfo.organization === orgInfo.org;
    });

    let repos = _.uniq(_.map(orgBuilds, function(build) {
      let latestBuild = (build.inProgressBuild ? build.inProgressBuild : build.lastBuild ? build.lastBuild : build.pendingBuild);
      latestBuild.module = build.module.name;
      latestBuild.branch = build.gitInfo.branch;
      let repo = {
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
    let masterIndex = branches.map(function(el) {
      return el.branch;
    }).indexOf('master');

    if (masterIndex > 0) {
      let master = branches.splice(masterIndex, masterIndex + 1);
      branches = master.concat(branches);
    }

    return branches;

  }





  groupBuildsByRepo() {
    // group and generate key, by org::repo[branch]


    // let grouped = _.groupBy(this.hasBuildState(), function(o) {
    let grouped = _.groupBy(this.data, function(o) {
      return `${o.gitInfo.organization}::${o.gitInfo.repository}[${o.gitInfo.branch}]`;
    });


    // Make note if a repo has ANY module building
    _.each(grouped, (repo) => {

      repo.moduleIsBuilding = false;

      for (let value in repo) {
        if (value.inProgressBuild) {
          repo.moduleIsBuilding = true;
          break;
        }
      }
      return repo;
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
