/*global config*/
import BaseCollection from './BaseCollection';

class BuildHistory extends BaseCollection {

  constructor(moduleId) {
    this.moduleId = moduleId;
  }

  url() {
    return `${config.apiRoot}/build/history/module/${this.moduleId}`;
  }


}

export default BuildHistory;
