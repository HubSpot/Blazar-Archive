/*global config*/
import BaseCollection from './BaseCollection';

class BuildHistory extends BaseCollection {

  url() {
    return `${config.apiRoot}/build/history/module/${this.options.moduleId}`;
  }
}

export default BuildHistory;
