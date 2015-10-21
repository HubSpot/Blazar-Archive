/*global config*/
import {has, uniq, sortBy, findWhere, groupBy} from 'underscore';
import BaseCollection from './BaseCollection';
import {cmp} from '../components/Helpers';

class Builds extends BaseCollection {

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
    
    let url = `${config.apiRoot}/build/states`;

    params.forEach((prop, i) => {
      const seperator = i === 0 ? '?' : '&';
      url += `${seperator}property=${prop}`;
    });
  
    switch (this.options.request) {
      case 'starred':
        this.options.stars.forEach((star) => {
          url += `&moduleId=${star.moduleId}`
        });
        url+= '&since=0';
        break;

      case 'all':
        url+= `&since=${this.updatedTimestamp}`;
        break;
        
      case 'branches':
        url = `${config.apiRoot}/branch/search?&host=${this.options.params.host}`;
        url += `&organization=${this.options.params.org}`;
        url += `&repository=${this.options.params.repo}`;
        url += `&property=id&since=${this.updatedTimestamp}`;

      case 'building':
        url += '&since=0&buildState=LAUNCHING&buildState=IN_PROGRESS'
        break;
        
      case 'branchIds':
        this.options.branchIds.forEach((m) => {
          url += `&branchId=${m.id}`
        });
        url += `&since=${this.updatedTimestamp}`
        break;
    }

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


  getHosts() {
    return this._orgsByHost(this._uniqueProperty(this.data, 'host'));
  }

  getReposByOrg() {
    const orgBuilds = this._hasBuildState();

    const repos = orgBuilds.map((build) => {
      const {
        gitInfo,
        lastBuild,
        module,
        inProgressBuild,
        pendingBuild
      } = build;
      
      let latestBuild;
      let startTime;

      if (has(build, 'inProgressBuild')) {
        latestBuild = inProgressBuild;
        startTime = inProgressBuild.startTimestamp;
      }
    
      // dont include for now as we dont have
      // a timestamp to sort by
      else if (has(build, 'pendingBuild')) {
        latestBuild = pendingBuild
      }

      else if (has(build, 'lastBuild')) {
        latestBuild = lastBuild;
        startTime = lastBuild.startTimestamp ? lastBuild.startTimestamp : lastBuild.endTimestamp;
      }

      else {
        return false
      }

      if (startTime === 'undefined') {
        return false;
      }

      latestBuild.module = module.name;
      latestBuild.branch = gitInfo.branch;

      const repoBlazarPath = `${config.appRoot}/builds/${gitInfo.host}/${gitInfo.organization}/${gitInfo.repository}`;
      const repo = {
        module: module.name,
        repo: gitInfo.repository,
        organization: gitInfo.organization,
        host: gitInfo.host,
        startTime: startTime,
        latestBuild: latestBuild,
        blazarPath: {
          repoBlazarPath: repoBlazarPath
        }
      };
      
      return repo;
    });

    repos.sort( (a, b) => {
      return cmp(b.repo.toLowerCase(), a.repo.toLowerCase()) || cmp(b.startTime, a.startTime);
    });

    const uniqRepos = uniq(repos, (r) => {
      return r.repo;
    });

    return sortBy(uniqRepos, function(r) {
      return r.repo.toLowerCase();
    });
  }


  sortByModuleName() {    
    return sortBy(this.data, (m) => {
      return m.module.name.toLowerCase();
    });
  }

  sortByBranchName() {
    return sortBy(this.data, (m) => {
      return m.gitInfo.branch.toLowerCase();
    })
  }


}

export default Builds;
