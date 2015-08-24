import _ from 'underscore';
import BaseCollection from './BaseCollection';

class BranchModules extends BaseCollection {

  constructor(branchId){
    this.branchId = branchId;
  }

  url() {
    let branchId = this.branchId;
    return `${config.apiRoot}branch/${branchId}/modules`;
  }

}

export default BranchModules;
