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

  getUrl() {
    const params = [
      'gitInfo',
      'module.id',
      'module.name',
      'lastBuild.id',
      'lastBuild.buildNumber',
      'lastBuild.state',
      'lastBuild.startTimestamp',
      'lastBuild.endTimestamp',
      'lastBuild.sha',
      'inProgressBuild.id',
      'inProgressBuild.buildNumber',
      'inProgressBuild.state',
      'inProgressBuild.startTimestamp',
      'inProgressBuild.endTimestamp',
      'inProgressBuild.sha',
      'pendingBuild.id',
      'pendingBuild.buildNumber',
      'pendingBuild.state',
      'pendingBuild.startTimestamp',
      'pendingBuild.endTimestamp',
      'pendingBuild.sha'
    ];

    let url = `${config.apiRoot}/build/states?since=${this.updatedTimestamp}`;

    params.forEach((prop) => {
      url += `&property=${prop}`;
    });

    return url;
  }

  url() {
    return this.getUrl();
  }

  _hasBuildState() {
    return this.data.filter((item) => {
      return has(item, 'lastBuild') || has(item, 'inProgressBuild') || has(item, 'pendingBuild');
    });
  }

  _OrgBuildsByHost(host) {
    return this.data.filter((build) => {
      return build.gitInfo.host === host;
    });
  }

  _uniqueProperty(builds, prop) {
    const uniqBuilds = uniq(builds, (build) => {
      return build.gitInfo[prop];
    });

    return uniqBuilds.map((item) => {
      return item.gitInfo[prop];
    });
  }

  // To do: add blazarPath object for org links
  _orgsByHost(hosts) {
    return hosts.map((host) => {
      const filterOrgBuilds = this._OrgBuildsByHost(host);
      const uniqueOrgs = this._uniqueProperty(filterOrgBuilds, 'organization');
      const orgsWithDetail = uniqueOrgs.map((org) => {
        return {
          name: org,
          blazarPath: `${config.appRoot}/builds/${host}/${org}`
        };
      });

      return {
        name: host,
        orgs: orgsWithDetail
      };
    });
  }

  _groupBuildsByRepo() {
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

  getHosts() {
    return this._orgsByHost(this._uniqueProperty(this.data, 'host'));
  }

  getReposByOrg(params) {
    const builds = this._hasBuildState();
    const orgBuilds = builds.filter((build) => {
      return (build.gitInfo.organization === params.org) && (build.gitInfo.host === params.host);
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
    const modules = findWhere(this._groupBuildsByRepo(), {
      organization: branchInfo.org,
      repository: branchInfo.repo,
      branch: branchInfo.branch
    });

    return modules || {};
  }

  getBranchesByRepo(params) {
    const groupedBuilds = this._groupBuildsByRepo();
    let branches = groupedBuilds.filter((repo) => {
      return (repo.organization === params.org) && (repo.repository === params.repo);
    });

    return sortBy(branches, function(b) {
      return b.branch.toLowerCase();
    });

  }


}

export default Builds;
