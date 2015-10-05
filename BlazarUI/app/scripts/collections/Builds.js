/*global config*/
import {has, uniq, sortBy, findWhere, groupBy} from 'underscore';
import BaseCollection from './BaseCollection';

class Builds extends BaseCollection {

  constructor() {
    this.updatedTimestamp = 0;
  }

  parse() {
    super.parse();
  }

  url() {
    return `${config.apiRoot}/build/states?property=!lastBuild.commitInfo&property=!inProgressBuild.commitInfo&property=!pendingBuild.commitInfo&since=${this.updatedTimestamp}`;
  }

  hasBuildState() {
    return this.data.filter((item) => {
      return has(item, 'lastBuild') || has(item, 'inProgressBuild') || has(item, 'pendingBuild');
    });
  }

  getReposByOrg(orgInfo) {
    const builds = this.hasBuildState();
    const orgBuilds = builds.filter((build) => {
      return (build.gitInfo.organization === orgInfo.org) && (build.gitInfo.host === orgInfo.host);
    });

    const repos = uniq(orgBuilds.map((build) => {
      const {
        gitInfo,
        lastBuild,
        module,
        inProgressBuild,
        pendingBuild
      } = build;

      let latestBuild = (inProgressBuild ? inProgressBuild : lastBuild ? lastBuild : pendingBuild);
      latestBuild.module = module.name;
      latestBuild.branch = gitInfo.branch;

      const repoBlazarPath = `${config.appRoot}/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}`;
      const repo = {
        repo: gitInfo.repository,
        host: gitInfo.host,
        organization: gitInfo.organization,
        latestBuild: latestBuild,
        blazarPath: {
          repoBlazarPath: repoBlazarPath
        }
      };
      return repo;
    }), false, function(r) {
      return r.repo;
    });
    return sortBy(repos, function(r) {
      return r.repo.toLowerCase();
    });
  }

  getBranchModules(branchInfo) {
    const modules = findWhere(this.groupByRepo(), {
      organization: branchInfo.org,
      repository: branchInfo.repo,
      branch: branchInfo.branch
    });

    return modules || {};
  }

  getBranchesByRepo(repoInfo) {
    const groupedBuilds = this.groupByRepo();
    let branches = groupedBuilds.filter((repo) => {
      return (repo.organization === repoInfo.org) && (repo.repository === repoInfo.repo);
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

  groupByRepo() {
    // group and generate key, by org::repo[branch]
    const grouped = groupBy(this.data, function(o) {
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

        if (has(module, 'lastBuild')) {
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
