import _ from 'underscore';
import BaseCollection from './BaseCollection';

class Builds extends BaseCollection {

  url() {
    return "/api/build/states";
  }

  parse() {
    this.addTimeHelpers();
  }

  // list of module names, used for sidebar search
  getModuleList() {
    let modules = _.map(this.data, function(item) {

      let {gitInfo, module, buildState} = item;

      let moduleInfo = {
        repository: gitInfo.repository,
        module: module.name,
        link: `${config.appRoot}/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}/${gitInfo.branch}/${module.name}/${buildState === undefined ? '' : buildState.buildNumber}`
      };
      return moduleInfo;
    });

    return modules;
  }

  getBranchModules(branchInfo) {
    let groupBuilds = this.groupBuildsByRepo()

    let repo = _.findWhere(groupBuilds, {
      organization: branchInfo.org,
      repository: branchInfo.repo
    })

    return _.findWhere(groupBuilds, {
      organization: branchInfo.org,
      repository: branchInfo.repo,
      branch: branchInfo.branch
    })

  }

  getBranchesByRepo(repoInfo){
    let groupBuilds = this.groupBuildsByRepo()
    let branches = _.filter(groupBuilds, (repo) => {
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
    // group/generate key, by org::repo[branch]
    let grouped = _.groupBy(this.data, function(o) {
      return `${o.gitInfo.organization}::${o.gitInfo.repository}[${o.gitInfo.branch}]`;
    });

    // Make note if a repo has any module building
    _.each(grouped, (repo) => {
      repo.moduleIsBuilding = false;
      for (var value of repo) {
        if(value.buildState !== undefined && value.buildState.result === 'IN_PROGRESS'){
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
      if (repo.modules[0].buildState !== undefined) {
        repo.mostRecentBuild = repo.modules[0].buildState.startTime;
      }
      repo.host = repo.modules[0].gitInfo.host;
      repo.branch = repo.modules[0].gitInfo.branch;
      repo.organization = repo.modules[0].gitInfo.organization;
      repo.id = `${repo.host}_${repo.branch}_${repo.organization}_${repo.repository}`;
      repo.branchPath = `${config.appRoot}/builds/${repo.modules[0].gitInfo.host}/${repo.modules[0].gitInfo.organization}/${repo.modules[0].gitInfo.repository}/${repo.modules[0].gitInfo.branch}`;

      repo.modules.forEach( (module) => {
        module.modulePath = `${config.appRoot}/builds/${module.gitInfo.host}/${module.gitInfo.organization}/${module.gitInfo.repository}/${module.gitInfo.branch}/${module.module.name}`;
        if (module.buildState !== undefined) {
          module.buildState.buildLink = `${config.appRoot}/builds/${module.gitInfo.host}/${module.gitInfo.organization}/${module.gitInfo.repository}/${module.gitInfo.branch}/${module.module.name}/${module.buildState.buildNumber}`;
          if (module.buildState.startTime < repo.mostRecentBuild) {
            repo.mostRecentBuild = module.buildState.startTime;
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
