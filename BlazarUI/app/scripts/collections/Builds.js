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

  // list of module names, used for sidebar search
  getModuleList() {
    return _.map(this.hasBuildState(), function(item) {

      let {gitInfo, module, lastBuild, inProgressBuild, pendingBuild} = item;

      let moduleInfo = {
        repository: gitInfo.repository,
        branch: gitInfo.branch,
        module: module.name,
        link: `${config.appRoot}/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${gitInfo.branch}/${module.name}_${module.id}`
      };

      if (lastBuild) {
        moduleInfo.lastBuildState = lastBuild.state;
      }

      if (inProgressBuild) {
        moduleInfo.inProgressBuildLink = `${config.appRoot}/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${gitInfo.branch}/${module.name}_${module.id}/${inProgressBuild.buildNumber}`;
      }

      if (pendingBuild) {
        moduleInfo.pendingBuild = true
      }

      return moduleInfo;
    });

  }

  getReposByOrg(orgInfo) {
    let builds = this.hasBuildState();
    let orgBuilds = _.filter(builds, function(a) {
      return a.gitInfo.organization === orgInfo.org
    });

    let repos = _.uniq(_.map(orgBuilds, function (build) {
      let latestBuild = (build.inProgressBuild ? build.inProgressBuild : build.lastBuild ? build.lastBuild : build.pendingBuild);
      latestBuild.module = build.module.name
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
    return _.findWhere(this.groupBuildsByRepo(), {
      organization: branchInfo.org,
      repository: branchInfo.repo,
      branch: branchInfo.branch
    })
  }

  getBranchesByRepo(repoInfo){
    let branches = _.filter(this.groupBuildsByRepo(), (repo) => {
      return repo.organization === repoInfo.org && repo.repository === repoInfo.repo;
    })

    branches.sort( (a, b) => {
      return b.branch - a.branch
    })

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
    let grouped = _.groupBy(this.hasBuildState(), function(o) {
      return `${o.gitInfo.organization}::${o.gitInfo.repository}[${o.gitInfo.branch}]`;
    });

    // Make note if a repo has ANY module building
    _.each(grouped, (repo) => {

      repo.moduleIsBuilding = false;

      for (var value of repo) {
        if(value.inProgressBuild){
          repo.moduleIsBuilding = true;
          break;
        }
      }
      return repo;
    })

    // move groupedBy object into an easier-to-work-with array
    let groupedInArray = [];
    for (var repo in grouped) {
      groupedInArray.push({
        repoModuleKey: repo,
        repository: grouped[repo][0].gitInfo.repository,
        isBuilding: grouped[repo].moduleIsBuilding,
        modules: grouped[repo]
      })
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

      repo.modules.forEach( (module) => {
        module.modulePath = `${config.appRoot}/builds/${module.gitInfo.host}/${module.gitInfo.organization}/${module.gitInfo.repository}/${module.gitInfo.branch}/${module.module.name}`;
        if (module.inProgressBuild) {
          module.inProgressBuild.buildLink = `${config.appRoot}/builds/${module.gitInfo.host}/${module.gitInfo.organization}/${module.gitInfo.repository}/${module.gitInfo.branch}/${module.module.name}/${module.inProgressBuild.buildNumber}`;
          if (module.inProgressBuild.startTimestamp < repo.mostRecentBuild) {
            repo.mostRecentBuild = module.inProgressBuild.startTimestamp;
          }
        }
      })
    })

    // sort by if building then by most recent build time
    function cmp(x, y) {
      return x > y ? 1 : x < y ? -1 : 0;
    }

    groupedInArray.sort( (a,b) => { ;
      return cmp(b.isBuilding, a.isBuilding) || cmp(a.mostRecentBuild, b.mostRecentBuild);
    })

    return groupedInArray;
  }


}

export default Builds;
