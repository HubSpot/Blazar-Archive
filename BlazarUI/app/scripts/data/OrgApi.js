import { findWhere, uniq, sortBy, filter } from 'underscore';
import { fromJS } from 'immutable';
import StoredBuilds from './StoredBuilds';

class OrgApi extends StoredBuilds {

  _parse() {
    const {params} = this.options;

    // get unique repos 
    const repos = uniq(this.builds.filter((build) => {
      return build.gitInfo.branch === 'master';
    }).map((build) => {
      return {
        organization: build.gitInfo.organization,
        repository: build.gitInfo.repository,
        blazarRepositoryPath: build.gitInfo.blazarRepositoryPath,
        gitInfo: build.gitInfo,
        lastBuild: build.lastBuild
      };
    }), (build) => {
      return build.repository;
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
