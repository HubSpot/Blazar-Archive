/*global config*/
import BaseCollection from './BaseCollection';

class BranchModules extends BaseCollection {

  constructor(branchId) {
    this.branchId = branchId;
  }

  url() {
    const branchId = this.branchId;
    return `${config.apiRoot}/branch/${branchId}/modules`;
  }

}

export default BranchModules;
