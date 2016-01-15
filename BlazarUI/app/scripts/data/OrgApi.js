import { findWhere, uniq } from 'underscore';
import { fromJS } from 'immutable';
import StoredBuilds from './StoredBuilds';

class OrgApi extends StoredBuilds {

  _parse() {
    const {params} = this.options;
    
    const repos = uniq(this.builds.toJS().map((build) => {
      return {
        repository: build.gitInfo.repository,
        blazarRepositoryPath: build.gitInfo.blazarRepositoryPath
      }
    }), (b) => {
      return b.repository;
    });

    this.cb(fromJS(repos));
  }

}

export default OrgApi;
