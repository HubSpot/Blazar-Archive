/*global config*/
import BaseCollection from './BaseCollection';

class BuildHistory extends BaseCollection {

  constructor(module) {
    this.module = module;
  }

  url() {
    let moduleId = this.module.moduleId;
    return `${config.apiRoot}/build/history/module/${moduleId}`;
  }

  parse() {
    this.addTimeHelpers();
  }
}

export default BuildHistory;
