/*global config*/
import BaseCollection from './BaseCollection';

class BranchModules extends BaseCollection {

  url() {
    const branchId = this.options.branchId;
    return `${config.apiRoot}/branch/${this.options.branchId}/modules`;
  }

}

export default BranchModules;
