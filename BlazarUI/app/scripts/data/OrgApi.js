import { findWhere, uniq, sortBy, filter } from 'underscore';
import { fromJS } from 'immutable';
import StoredBuilds from './StoredBuilds';

class OrgApi extends StoredBuilds {

  _parse() {
    const {params} = this.options;

    console.log("builds: ", this.builds.toJS());
    
    let branchMap = {};

    // get unique repos 
    const repos = uniq(this.builds.toJS().map((build) => {

      if (build.gitInfo.active) {
        if (branchMap[build.gitInfo.repository] === undefined) {
          branchMap[build.gitInfo.repository] = 1;
        }

        else {
          branchMap[build.gitInfo.repository] = branchMap[build.gitInfo.repository] + 1;
        }
      }

      return {
        organization: build.gitInfo.organization,
        repository: build.gitInfo.repository,
        blazarRepositoryPath: build.gitInfo.blazarRepositoryPath,
        lastBuild: build.lastBuild
      };
    }), (b) => {
      return b.repository;
    }).map((repo) => {
      repo['branchCount'] = branchMap[repo.repository] !== undefined ? branchMap[repo.repository] : 0;
      return repo;
    });

    // filter by org
    const reposFiltered = repos.filter((repo) => {
      return repo.organization === params.org;
    });
    
    const sortedRepos = sortBy(reposFiltered, (r) => {
      return r.repository.toLowerCase();
    });

    this.cb(fromJS(sortedRepos));
  }

}

export default OrgApi;
