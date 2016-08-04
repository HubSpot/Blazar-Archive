/* global config*/
import { findWhere } from 'underscore';
import StoredBuilds from './StoredBuilds';

class BranchesApi extends StoredBuilds {

  _parse() {
    const {params} = this.options;

    const branches = this.builds.filter((branch) => {
      return branch.gitInfo.repository.toLowerCase() === params.repo.toLowerCase();
    });

    this.cb(branches);
  }

}

export default BranchesApi;
